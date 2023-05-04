package com.se.advancedweb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.se.advancedweb.entity.User;
import com.se.advancedweb.entity.UserLoginHistory;
import com.se.advancedweb.mapper.UserLoginHistoryMapper;
import com.se.advancedweb.mapper.UserMapper;
import com.se.advancedweb.service.UserService;
import com.se.advancedweb.util.ConstVariable;
import com.se.advancedweb.util.Response;
import com.se.advancedweb.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private UserMapper userMapper;
    private UserLoginHistoryMapper userLoginHistoryMapper;
    @Autowired
    public UserServiceImpl(UserMapper userMapper, UserLoginHistoryMapper userLoginHistoryMapper) {
        this.userMapper = userMapper;
        this.userLoginHistoryMapper = userLoginHistoryMapper;
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
        String token  = TokenUtil.getToken(String.valueOf(user.getUserId()) , user.getUsername(), String.valueOf(user.getRole()), user.getPassword());
        data.put("token", token);
        //添加用户登陆记录
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        UserLoginHistory userLoginHistory = new UserLoginHistory(timestamp, ConstVariable.LOGIN_OPERATION, user);
        userLoginHistoryMapper.save(userLoginHistory);
        return new Response<>(true, "登陆成功！", data);
    }
    @Override
    public Response<?> getUserInfo(String token) {
        JSONObject data = new JSONObject();
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findByUserId(Integer.parseInt(id));
        //更新token
        String newToken = TokenUtil.getToken(id, user.getUsername(), String.valueOf(user.getRole()), user.getPassword());
        data.put("token", newToken);
        data.put("user_name", user.getUsername());
        data.put("role", user.getRole());
        return new Response<>(true, "获取用户信息成功！", data);
    }
    @Override
    public Response<?> register(String username, String password, int role) {
        User user = userMapper.findByUsername(username);
        if(user != null){
            return new Response<>(false, "用户名已存在！");
        }
        user = new User(username, password, role);

        userMapper.save(user);
        return new Response<>(true, "注册成功！");
    }
    @Override
    public List<User> getAllUser() {
        return userMapper.findAll();
    }
    @Override
    public User findUserById(String id) {
        return userMapper.findByUserId(Integer.parseInt(id));
    }
    @Override
    public Response<?> logout(String token){
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findByUserId(Integer.parseInt(id));
        //添加用户登出记录
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        UserLoginHistory userLoginHistory = new UserLoginHistory(timestamp, ConstVariable.LOGOUT_OPERATION, user);
        userLoginHistoryMapper.save(userLoginHistory);
        return new Response<>(true, "登出成功！");
    }
}