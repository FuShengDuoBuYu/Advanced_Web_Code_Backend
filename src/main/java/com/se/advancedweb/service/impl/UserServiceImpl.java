package com.se.advancedweb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.se.advancedweb.entity.User;
import com.se.advancedweb.mapper.UserMapper;
import com.se.advancedweb.service.UserService;
import com.se.advancedweb.util.Response;
import com.se.advancedweb.util.TokenUtil;
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
    public Response<?> login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if(user == null){
            return new Response<>(false, "用户不存在！");
        }
        if(!user.getPassword().equals(password)){
            return new Response<>(false, "密码错误！");
        }
        JSONObject data = new JSONObject();
        String token  = TokenUtil.getToken(String.valueOf(user.getId()) , user.getUsername(), String.valueOf(user.getRole()), user.getPassword());
        data.put("token", token);
        return new Response<>(true, "登陆成功！", data);
    }
    @Override
    public Response<?> getUserInfo(String token) {
        JSONObject data = new JSONObject();
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findById(id);
        //更新token
        String newToken = TokenUtil.getToken(id, user.getUsername(), String.valueOf(user.getRole()), user.getPassword());
        data.put("token", newToken);
        data.put("user_name", user.getUsername());
        data.put("role", user.getRole());
        return new Response<>(true, "获取用户信息成功！", data);
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