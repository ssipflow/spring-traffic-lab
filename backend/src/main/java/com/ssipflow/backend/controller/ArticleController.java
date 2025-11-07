package com.ssipflow.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ssipflow.backend.dto.EditArticleDto;
import com.ssipflow.backend.dto.WriteArticleDto;
import com.ssipflow.backend.entity.Article;
import com.ssipflow.backend.service.ArticleService;
import com.ssipflow.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/boards")
public class ArticleController {

    private final AuthenticationManager authenticationManager;
    private final ArticleService articleService;
    private final CommentService commentService;

    @PostMapping("/{boardId}/articles")
    public ResponseEntity<Article> writeArticle(@AuthenticationPrincipal UserDetails userDetails,
                                                @PathVariable Long boardId,
                                                @RequestBody WriteArticleDto writeArticleDto) throws JsonProcessingException {
        return ResponseEntity.ok(articleService.writeArticle(boardId, writeArticleDto, userDetails));
    }

    @GetMapping("/{boardId}/articles")
    public ResponseEntity<List<Article>> getArticle(@PathVariable Long boardId,
                                                    @RequestParam(required = false) Long lastId,
                                                    @RequestParam(required = false) Long firstId) {
        if (lastId != null) {
            return ResponseEntity.ok(articleService.getOldArticle(boardId, lastId));
        } else if (firstId != null) {
            return ResponseEntity.ok(articleService.getNewArticle(boardId, firstId));
        }
        return ResponseEntity.ok(articleService.firstGetArticle(boardId));
    }

    @GetMapping("/{boardId}/articles/search")
    public ResponseEntity<List<Article>> searchArticles(@PathVariable Long boardId,
                                                        @RequestParam(required = false) String keyword) {
        if (keyword != null) {
            return ResponseEntity.ok(articleService.searchArticle(keyword));
        }
        return ResponseEntity.ok(articleService.firstGetArticle(boardId));
    }

    @PutMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> editArticle(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable Long boardId,
                                               @PathVariable Long articleId,
                                               @RequestBody EditArticleDto editArticleDto) throws JsonProcessingException {
        return ResponseEntity.ok(articleService.editArticle(boardId, articleId, editArticleDto, userDetails));
    }

    @DeleteMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<String> deleteArticle(@AuthenticationPrincipal UserDetails userDetails,
                                                @PathVariable Long boardId,
                                                @PathVariable Long articleId) {
        articleService.deleteArticle(boardId, articleId, userDetails);
        return ResponseEntity.ok("Article deleted successfully");
    }

    @GetMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> getArticleWithComment(@PathVariable Long boardId,
                                                          @PathVariable Long articleId) {
        CompletableFuture<Article> article = articleService.getArticleWithComment(boardId, articleId);
        return ResponseEntity.ok(article.resultNow());
    }
}
