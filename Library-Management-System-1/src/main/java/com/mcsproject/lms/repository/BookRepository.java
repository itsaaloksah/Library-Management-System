//package com.mcsproject.lms.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import com.mcsproject.lms.entity.BookEntity;
//
//public interface BookRepository extends JpaRepository<BookEntity, Long> {
//
//}

package com.mcsproject.lms.repository;

import com.mcsproject.lms.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {
    
    // Custom query to find the total count of unique categories
    @Query("SELECT COUNT(DISTINCT b.category) FROM BookEntity b")
    long countDistinctCategories();
}