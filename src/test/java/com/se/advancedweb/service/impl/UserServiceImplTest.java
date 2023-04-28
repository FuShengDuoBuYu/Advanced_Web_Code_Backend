package com.se.advancedweb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.se.advancedweb.entity.User;
import com.se.advancedweb.mapper.UserMapper;
import com.se.advancedweb.util.Response;
import com.se.advancedweb.util.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class UserServiceImplTest {
    private User user;
    @BeforeEach
    void setUp() {
        // 创建一个模拟的 User 对象
        user = new User();
        user.setId(123);
        user.setUsername("TestUser");
        user.setRole(1);
        user.setPassword("password");

    }

    @Test
    public void Login_success() {
        // Arrange
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.findByUsername(Mockito.anyString())).thenReturn(user);
        UserServiceImpl userService = new UserServiceImpl(userMapper);
        // Act
        Response<?> response = userService.login("TestUser", "password");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("登陆成功！", response.getMessage());
        assertNotNull(response.getData());
        JSONObject data = (JSONObject)response.getData();
        assertTrue(data.containsKey("token"));
        String token = data.getString("token");
        String userId = JWT.decode(token).getAudience().get(0);
        assertEquals("123", userId);
    }
    @Test
    public void Login_user_not_exist() {
        // Arrange
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.findByUsername("testUser")).thenReturn(null);
        UserServiceImpl userService = new UserServiceImpl(userMapper);
        // Act
        Response<?> response = userService.login("testUser", "testPassword");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("用户不存在！", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void getUserInfo_success() {
        // 创建一个模拟的 Token
        String token = TokenUtil.getToken("123", "TestUser", "1", "password");

        // mock 掉 userMapper 的 findById 方法
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.findById(Mockito.anyInt())).thenReturn(user);
        UserServiceImpl userService = new UserServiceImpl(userMapper);
        // 调用 getUserInfo 方法获取响应
        Response<?> response = userService.getUserInfo(token);

        // 验证响应中包含正确的信息
        assertTrue(response.isSuccess());
        assertEquals("获取用户信息成功！", response.getMessage());
        JSONObject data = (JSONObject) response.getData();
        assertEquals("TestUser", data.getString("user_name"));
        assertEquals(1, data.getIntValue("role"));
    }
    @Test
    public void testRegisterWithNewUsername(){
        // mock 掉 userMapper 的 findById 方法
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.findByUsername(Mockito.anyString())).thenReturn(null);
        UserServiceImpl userService = new UserServiceImpl(userMapper);

        Response<?> response = userService.register("TestUser_new", "password_new", 1);
        assertTrue(response.isSuccess());
        assertEquals("注册成功！", response.getMessage());
        verify(userMapper, times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void testRegisterWithExistingUsername(){
        // mock 掉 userMapper 的 findById 方法
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.findByUsername(Mockito.anyString())).thenReturn(user);
        UserServiceImpl userService = new UserServiceImpl(userMapper);

        Response<?> response = userService.register("TestUser", "password", 1);
        assertFalse(response.isSuccess());
        assertEquals("用户名已存在！", response.getMessage());
    }

    @Test
    void getAllUser() {
        List<User> users = new ArrayList<>();
        users.add(user);
        // mock 掉 userMapper 的 findById 方法
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.findAll()).thenReturn(users);
        UserServiceImpl userService = new UserServiceImpl(userMapper);
        List<User> responseData = userService.getAllUser();
        assertEquals(1, responseData.size());
        assertEquals("TestUser", responseData.get(0).getUsername());
    }

    @Test
    void findUserById() {
        // mock 掉 userMapper 的 findById 方法
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.findById(Mockito.anyInt())).thenReturn(user);
        UserServiceImpl userService = new UserServiceImpl(userMapper);
        User responseData = userService.findUserById(String.valueOf(user.getId()));
        assertEquals("TestUser", responseData.getUsername());
    }
}