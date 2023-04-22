package com.se.advancedweb.controller;

import com.alibaba.fastjson.JSONObject;
import com.se.advancedweb.common.VerifyToken;
import com.se.advancedweb.service.UserService;
import com.se.advancedweb.util.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "用户登陆 ")
    @PostMapping("/login")
    public Response<?> login(@RequestBody JSONObject body) {
        String username = body.getString("username");
        String password = body.getString("password");
        return userService.login(username, password);
    }
    @ApiOperation(value = "获取用户信息")
    @VerifyToken
    @PostMapping("/info")
    public Response<?> getUserInfo(@RequestHeader("token") String token) {
        return userService.getUserInfo(token);
    }
    @ApiOperation(value = "获取所有用户")
    @VerifyToken
    @GetMapping("/all")
    public String helloUser() {
        return userService.getAllUser().toString();
    }
}
