package com.se.advancedweb.controller;

import com.alibaba.fastjson.JSONObject;
import com.se.advancedweb.common.VerifyToken;
import com.se.advancedweb.service.UserService;
import com.se.advancedweb.util.Response;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "用户登陆 ")
    // 在swagger中加入body的参数示例
    @PostMapping("/login")
    public Response<?> login(@RequestBody JSONObject body) {
        String username = body.getString("username");
        String password = body.getString("password");
        return userService.login(username, password);
    }
    @ApiOperation(value = "获取用户信息", hidden = true)
    @VerifyToken
    @GetMapping("/info")
    public Response<?> getUserInfo(@RequestHeader("token") String token) {
        return userService.getUserInfo(token);
    }
    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public Response<?> register(@RequestBody
                                JSONObject body) {
        String username = body.getString("username");
        String password = body.getString("password");
        int role = body.getInteger("role");
        String avatarBase64 = body.getString("avatarBase64");
        return userService.register(username, password, role, avatarBase64);
    }
    @ApiOperation(value = "用户修改密码")
    @VerifyToken
    @PostMapping("/changePassword")
    public Response<?> changePassword(@RequestHeader("token") String token, @RequestBody JSONObject body) {
        String oldPassword = body.getString("oldPassword");
        String newPassword = body.getString("newPassword");
        return userService.changePassword(token, oldPassword, newPassword);
    }

    @ApiOperation(value = "用户修改个人信息")
    @VerifyToken
    @PostMapping("/changeUserInfo")
    public Response<?> changeUserInfo(@RequestHeader("token") String token, @RequestBody JSONObject body) {
        String username = body.getString("username");
        String avatarBase64 = body.getString("avatarBase64");
        return userService.changeUserInfo(token, username, avatarBase64);
    }

    @ApiOperation(value = "获取所有用户", hidden = true)
    @VerifyToken
    @GetMapping("/all")
    public Response<?> helloUser() {
        return userService.getAllUser();
    }

    @ApiOperation(value = "用户登出")
    @VerifyToken
    @PostMapping("/logout")
    public Response<?> logout(@RequestHeader("token") String token) {
        return userService.logout(token);
    }

    @ApiOperation(value = "老师新建课程")
    @VerifyToken
    @PostMapping("/createCourse")
    public Response<?> createCourse(@RequestHeader("token") String token, @RequestBody JSONObject body) {
        String courseName = body.getString("courseName");
        String courseDescription = body.getString("courseDescription");
        String building = body.getString("building");
        int isOver = body.getInteger("isOver");
        return userService.createCourse(token, courseName, courseDescription, building, isOver);
    }
    @ApiOperation(value = "老师删除课程")
    @VerifyToken
    @DeleteMapping("/deleteCourse")
    public Response<?> deleteCourse(@RequestHeader("token") String token, @RequestBody JSONObject body) {
        String courseName = body.getString("courseName");
        return userService.deleteCourse(token, courseName);
    }

    @ApiOperation(value = "通过教学楼获取课程")
    @GetMapping("/getCourseByBuilding/{building}")
    public Response<?> getCourseByBuilding(@PathVariable("building") String building) {
        return userService.getCourseByBuilding(building);
    }

    @ApiOperation(value = "学生加入课程")
    @VerifyToken
    @PostMapping("/joinCourse")
    public Response<?> joinCourse(@RequestHeader("token") String token, @RequestBody JSONObject body) {
        String courseName = body.getString("courseName");
        return userService.joinCourse(token, courseName);
    }

    @ApiOperation(value = "获取用户课程")
    @VerifyToken
    @GetMapping("/getCourse")
    public Response<?> getCourse(@RequestHeader("token") String token) {
        return userService.getCourse(token);
    }

    @ApiOperation(value = "获取用户在线时间")
    @VerifyToken
    @GetMapping("/getConnectDuration")
    public Response<?> getConnectDuration(@RequestHeader("token") String token) {
        return userService.getConnectDuration(token);
    }
    @ApiOperation(value = "获取所有用户在线时间")
    @VerifyToken
    @GetMapping("/getAllConnectDuration")
    public Response<?> getAllConnectDuration() {
        return userService.getAllConnectDuration();
    }

    @ApiOperation(value = "获取上一周学习时长")
    @VerifyToken
    @GetMapping("/chart/getSevenDaysDuration")
    public Response<?> getStudyDuration(@RequestHeader("token") String token) {
        return userService.getSevenDaysDuration(token);
    }
    @ApiOperation(value = "获取每门课的发言次数")
    @VerifyToken
    @GetMapping("/chart/getCourseChatTimes")
    public Response<?> getCourseChatTimes(@RequestHeader("token") String token) {
        return userService.getCourseChatTimes(token);
    }

}
