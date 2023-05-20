package com.se.advancedweb.mapper;

import com.se.advancedweb.entity.UserChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChatMessageMapper extends JpaRepository<UserChatMessage, Long> {

}
