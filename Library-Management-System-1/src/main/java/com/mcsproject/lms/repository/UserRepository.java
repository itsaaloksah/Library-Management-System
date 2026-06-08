package com.mcsproject.lms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mcsproject.lms.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

//    User findByUsername(String username);
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
}