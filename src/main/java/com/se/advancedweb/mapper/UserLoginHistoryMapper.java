package com.se.advancedweb.mapper;

import com.se.advancedweb.entity.User;
import com.se.advancedweb.entity.UserLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLoginHistoryMapper extends JpaRepository<UserLoginHistory, Long>{
    List<UserLoginHistoryMapper> findByUser(User user);
}
