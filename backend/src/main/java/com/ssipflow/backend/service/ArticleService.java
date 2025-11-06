package com.ssipflow.backend.service;

import com.ssipflow.backend.dto.EditArticleDto;
import com.ssipflow.backend.dto.WriteArticleDto;
import com.ssipflow.backend.entity.Article;
import com.ssipflow.backend.entity.Board;
import com.ssipflow.backend.entity.User;
import com.ssipflow.backend.exception.ForbiddenException;
import com.ssipflow.backend.exception.RateLimitException;
import com.ssipflow.backend.exception.ResourceNotFoundException;
import com.ssipflow.backend.repository.ArticleRepository;
import com.ssipflow.backend.repository.BoardRepository;
import com.ssipflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;


    public Article writeArticle(Long boardId, WriteArticleDto writeArticleDto, UserDetails userDetails) {
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        if (author.isEmpty()) {
            throw new ResourceNotFoundException("User with username " + userDetails.getUsername() + " not found");
        }

        if (!this.isCanEditArticle(author.get())) {
            throw new RateLimitException("Article can only be written once every 5 minutes");
        }

        Optional<Board> board = boardRepository.findById(boardId);
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("Board with id " + boardId + " not found");
        }

        Article article = new Article();
        article.setBoard(board.get());
        article.setAuthor(author.get());
        article.setTitle(writeArticleDto.getTitle());
        article.setContent(writeArticleDto.getContent());

        articleRepository.save(article);

        return article;
    }

    public List<Article> getOldArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(boardId, articleId);
    }

    public List<Article> getNewArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(boardId, articleId);
    }

    public List<Article> firstGetArticle(Long boardId) {
        return articleRepository.findTop10ByBoardIdOrderByCreatedDateDesc(boardId);
    }

    public List<Article> searchArticle(String keyword) {
        return null;
    }

    public Article editArticle(Long boardId, Long articleId, EditArticleDto dto, UserDetails userDetails) {
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        if (author.isEmpty()) {
            throw new ResourceNotFoundException("User with username " + userDetails.getUsername() + " not found");
        }

        Optional<Board> board = boardRepository.findById(boardId);
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("Board with id " + boardId + " not found");
        }

        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty()) {
            throw new ResourceNotFoundException("Article with id " + articleId + " not found");
        }

        if (!article.get().getAuthor().getUsername().equals(author.get().getUsername())) {
            throw new ForbiddenException("User is not the author of the article");
        }

        if (!this.isCanEditArticle(author.get())) {
            throw new RateLimitException("Article can only be edited once every 5 minutes");
        }

        if (dto.getTitle() != null) {
            article.get().setTitle(dto.getTitle());
        }

        if (dto.getContent() != null) {
            article.get().setContent(dto.getContent());
        }

        articleRepository.save(article.get());

        return article.get();
    }

    public void deleteArticle(Long boardId, Long articleId) {
    }

    public CompletableFuture<Article> getArticleWithComment(Long boardId, Long articleId) {
        return null;
    }

    private boolean isCanEditArticle(User author) {
        Article latestArticle = articleRepository.findLatestArticleByAuthorUsernameOrderByCreatedDate(author.getUsername());
        if (latestArticle == null || latestArticle.getUpdatedDate() == null) {
            return true;
        }

        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getUpdatedDate());
    }

    private boolean isDifferenceMoreThanFiveMinutes(LocalDateTime localDateTime) {
        LocalDateTime dateAsLocalDateTime = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(localDateTime, dateAsLocalDateTime);

        return Math.abs(duration.toMinutes()) > 5;
    }
}
