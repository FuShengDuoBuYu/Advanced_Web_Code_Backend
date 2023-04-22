package com.se.advancedweb.service.impl;

import com.se.advancedweb.entity.User;
import com.se.advancedweb.mapper.UserMapper;
import com.se.advancedweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<User> getAllUser() {
        return userMapper.findAll();
    }
    @Override
    public User findUserById(String id) {
        return userMapper.findById(id);
    }
}