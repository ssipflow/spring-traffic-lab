package com.ssipflow.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob    // text 타입 매핑을 위해 @Lob 어노테이션 사용
    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)) // 외래 키 제약 조건 비활성화
    private User author;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)) // 외래 키 제약 조건 비활성화
    private Board board;

    @OneToMany
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)) // 외래 키 제약 조건 비활성화
    private List<Comment> comments =new ArrayList<>();

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(insertable = true)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
