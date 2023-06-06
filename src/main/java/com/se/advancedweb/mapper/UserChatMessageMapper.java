package com.se.advancedweb.mapper;

import com.se.advancedweb.entity.User;
import com.se.advancedweb.entity.UserChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChatMessageMapper extends JpaRepository<UserChatMessage, Long> {
    List<UserChatMessage> findByUser(User user);
}
