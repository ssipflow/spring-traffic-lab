package com.ssipflow.backend.service;

import com.ssipflow.backend.dto.EditArticleDto;
import com.ssipflow.backend.dto.WriteArticleDto;
import com.ssipflow.backend.entity.Article;
import com.ssipflow.backend.entity.Board;
import com.ssipflow.backend.entity.User;
import com.ssipflow.backend.exception.ResourceNotFoundException;
import com.ssipflow.backend.repository.ArticleRepository;
import com.ssipflow.backend.repository.BoardRepository;
import com.ssipflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

    public List<Article> getOldArticle(Long boardId, Long lastId) {
        return null;
    }

    public List<Article> getNewArticle(Long boardId, Long firstId) {
        return null;
    }

    public List<Article> firstGetArticle(Long boardId) {
        return null;
    }

    public List<Article> searchArticle(String keyword) {
        return null;
    }

    public Article editArticle(Long boardId, Long articleId, EditArticleDto editArticleDto) {
        return null;
    }

    public void deleteArticle(Long boardId, Long articleId) {
    }

    public CompletableFuture<Article> getArticleWithComment(Long boardId, Long articleId) {
        return null;
    }
}
