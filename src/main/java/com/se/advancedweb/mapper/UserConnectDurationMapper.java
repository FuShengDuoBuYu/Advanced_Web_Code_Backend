package com.se.advancedweb.mapper;

import com.se.advancedweb.entity.User;
import com.se.advancedweb.entity.UserConnectDuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserConnectDurationMapper extends JpaRepository<UserConnectDuration, Long> {
    List<UserConnectDuration> findByUser(User user);
}
