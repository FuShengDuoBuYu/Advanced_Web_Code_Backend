package com.se.advancedweb.service;

import com.se.advancedweb.entity.User;

import java.util.List;

public interface UserService {
    // 获取所有用户
     public List<User> getAllUser();
    // 通过id获取用户
    public User findUserById(String id);
}
