package com.ssipflow.backend.repository;

import com.ssipflow.backend.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a " +
            "FROM Article a " +
            "WHERE a.board.id = :boardId AND a.isDeleted = false " +
            "ORDER BY a.createdDate DESC " +
            "LIMIT 10")
    List<Article> findTop10ByBoardIdOrderByCreatedDateDesc(@Param("boardId") Long boardId);

    @Query("SELECT a " +
            "FROM Article a " +
            "WHERE a.board.id = :boardId AND a.id < :articleId AND a.isDeleted = false " +
            "ORDER BY a.createdDate DESC " +
            "LIMIT 10")
    List<Article> findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(@Param("boardId") Long boardId,
                                                                               @Param("articleId") Long articleId);

    @Query("SELECT a " +
            "FROM Article a " +
            "WHERE a.board.id = :boardId AND a.id > :articleId AND a.isDeleted = false " +
            "ORDER BY a.createdDate DESC " +
            "LIMIT 10")
    List<Article> findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(@Param("boardId") Long boardId,
                                                                                  @Param("articleId") Long articleId);
}
