package com.se.advancedweb.service;

import com.se.advancedweb.entity.User;
import com.se.advancedweb.util.Response;

import java.util.List;

public interface UserService {
    //用户登陆
    public Response<?> login(String username, String password);
    // 获取用户信息
    public Response<?> getUserInfo(String token);
    // 获取所有用户
     public List<User> getAllUser();
    // 通过id获取用户
    public User findUserById(String id);
}
