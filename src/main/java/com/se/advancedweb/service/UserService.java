package com.se.advancedweb.service;

import com.se.advancedweb.entity.User;
import com.se.advancedweb.util.Response;

import java.util.List;

public interface UserService {
    //用户登陆
    public Response<?> login(String username, String password);
    // 获取用户信息
    public Response<?> getUserInfo(String token);
    // 用户注册
    public Response<?> register(String username, String password, int role);

    // 获取所有用户
     public Response<?> getAllUser();
    // 通过id获取用户
    public User findUserById(String id);

    public Response<?> logout(String token);

    public Response<?> createCourse(String token, String courseName, String courseDescription, String building, int isOver);
    public Response<?> getCourseByBuilding(String building);
    public Response<?> deleteCourse(String token, String courseName);
    public Response<?> joinCourse(String token, String courseName);

    public Response<?> getCourse(String token);

    public Response<?> getConnectDuration(String token);
    public Response<?> getAllConnectDuration();

}
