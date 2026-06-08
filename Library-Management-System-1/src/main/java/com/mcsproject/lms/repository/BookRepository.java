package com.mcsproject.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mcsproject.lms.entity.BookEntity;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

}