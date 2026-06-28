package com.mcsproject.lms.repository;

import com.mcsproject.lms.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {
    
    // Custom query to find the total count of unique categories
    @Query("SELECT COUNT(DISTINCT b.category) FROM BookEntity b")
    long countDistinctCategories();

    /**
     * Advanced case-insensitive partial keyword wildcard search matrix
     * Matches keywords across title, author, or category strings.
     */
    @Query("SELECT b FROM BookEntity b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BookEntity> searchBooksGlobal(@Param("keyword") String keyword);
}