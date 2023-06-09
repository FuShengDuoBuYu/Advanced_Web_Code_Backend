package com.se.advancedweb.mapper;

import com.se.advancedweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends JpaRepository<User, Long> {
    User findByUserId(int id);


    User findByUsername(String username);

    void deleteByUserId(String id);
}