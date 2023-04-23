package com.se.advancedweb.controller;

import com.alibaba.fastjson.JSONObject;
import com.se.advancedweb.common.VerifyToken;
import com.se.advancedweb.service.UserService;
import com.se.advancedweb.util.Response;
import io.swagger.annotations.*;
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
    // 在swagger中加入body的参数示例
    @PostMapping("/login")
    public Response<?> login(@RequestBody JSONObject body) {
        String username = body.getString("username");
        String password = body.getString("password");
        return userService.login(username, password);
    }
    @ApiOperation(value = "获取用户信息", hidden = true)
    @VerifyToken
    @PostMapping("/info")
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
        return userService.register(username, password, role);
    }
    @ApiOperation(value = "获取所有用户", hidden = true)
    @VerifyToken
    @GetMapping("/all")
    public String helloUser() {
        return userService.getAllUser().toString();
    }
}
