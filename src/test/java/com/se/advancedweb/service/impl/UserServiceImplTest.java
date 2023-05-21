package com.se.advancedweb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.se.advancedweb.entity.*;
import com.se.advancedweb.entity.VO.CourseStudentVO;
import com.se.advancedweb.entity.VO.CourseTeacherVO;
import com.se.advancedweb.mapper.*;
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
    private User student;
    private User teacher;
    private Course course;
    private List<Course> courseList;
    private CourseSelection courseSelection;
    private List<CourseSelection> courseSelectionList;
    private String studentToken;
    private String teacherToken;
    private UserLoginHistoryMapper userLoginHistoryMapper;
    private UserMapper userMapper;
    private UserServiceImpl userService;
    private CourseMapper courseMapper;
    private CourseSelectionMapper courseSelectionMapper;
    private UserChatMessageMapper userChatMessageMapper;
    private UserConnectDurationMapper userConnectDurationMapper;
    @BeforeEach
    void setUp() {
        // 创建一个模拟token
        studentToken = TokenUtil.getToken("123", "TestStudent", "1", "password");
        teacherToken = TokenUtil.getToken("123", "TestTeacher", "2", "password");
        // 创建一个模拟的 student 对象
        student = new User();
        student.setUserId(123);
        student.setUsername("TestStudent");
        student.setRole(1);
        student.setPassword("password");
        // 创建一个模拟的 teacher 对象
        teacher = new User();
        teacher.setUserId(123);
        teacher.setUsername("TestTeacher");
        teacher.setRole(2);
        teacher.setPassword("password");
        // 创建一个模拟的 Course 对象
        course = new Course();
        course.setCourseId(123);
        course.setCourseName("TestCourse");
        course.setTeacher(teacher);
        // 创建一个模拟的 Course列表
        courseList = new ArrayList<>();
        courseList.add(course);
        // 创建一个模拟的 CourseSelection 对象
        courseSelection = new CourseSelection();
        courseSelection.setCourseSelectionId(123);
        courseSelection.setCourse(course);
        courseSelection.setStudent(student);
        // 创建一个模拟的 CourseSelection 列表
        courseSelectionList = new ArrayList<>();
        courseSelectionList.add(courseSelection);

        // 创建一个模拟的 UserLoginHistoryMapper 对象
        userLoginHistoryMapper = Mockito.mock(UserLoginHistoryMapper.class);
        userMapper = Mockito.mock(UserMapper.class);
        courseMapper = Mockito.mock(CourseMapper.class);
        courseSelectionMapper = Mockito.mock(CourseSelectionMapper.class);
        userChatMessageMapper = Mockito.mock(UserChatMessageMapper.class);
        userConnectDurationMapper = Mockito.mock(UserConnectDurationMapper.class);

        userService = new UserServiceImpl(userMapper, userLoginHistoryMapper, courseMapper, courseSelectionMapper, userChatMessageMapper, userConnectDurationMapper);
    }

    @Test
    public void Login_success() {
        Mockito.when(userMapper.findByUsername(Mockito.anyString())).thenReturn(student);
        // Act
        Response<?> response = userService.login("TestStudent", "password");

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
        Mockito.when(userMapper.findByUsername("TestStudent")).thenReturn(null);
        // Act
        Response<?> response = userService.login("TestStudent", "testPassword");
        // Assert
        assertFalse(response.isSuccess());
        assertEquals("用户不存在！", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void getUserInfo_success() {
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(student);
        // 调用 getUserInfo 方法获取响应
        Response<?> response = userService.getUserInfo(studentToken);

        // 验证响应中包含正确的信息
        assertTrue(response.isSuccess());
        assertEquals("获取用户信息成功！", response.getMessage());
        JSONObject data = (JSONObject) response.getData();
        assertEquals("TestStudent", data.getString("user_name"));
        assertEquals(1, data.getIntValue("role"));
    }
    @Test
    public void testRegisterWithNewUsername(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUsername(Mockito.anyString())).thenReturn(null);

        Response<?> response = userService.register("TestUser_new", "password_new", 1);
        assertTrue(response.isSuccess());
        assertEquals("注册成功！", response.getMessage());
        verify(userMapper, times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void testRegisterWithExistingUsername(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUsername(Mockito.anyString())).thenReturn(student);

        Response<?> response = userService.register("TestStudent", "password", 1);
        assertFalse(response.isSuccess());
        assertEquals("用户名已存在！", response.getMessage());
    }

    @Test
    void getAllUser() {
        List<User> users = new ArrayList<>();
        users.add(student);
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findAll()).thenReturn(users);
        List<User> responseData = (List<User>) userService.getAllUser().getData();
        assertEquals(1, responseData.size());
        assertEquals("TestStudent", responseData.get(0).getUsername());
    }

    @Test
    void findUserById() {
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(student);
        User responseData = userService.findUserById(String.valueOf(student.getUserId()));
        assertEquals("TestStudent", responseData.getUsername());
    }

    @Test
    void logout(){
        // mock 掉 userMapper 的 findById 方法  以及 userLoginHistoryMapper 的 save 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(student);
        Mockito.when(userLoginHistoryMapper.save(Mockito.any())).thenReturn(new UserLoginHistory());
        Response<?> response = userService.logout(studentToken);
        assertTrue(response.isSuccess());
        assertEquals("登出成功！", response.getMessage());
        verify(userLoginHistoryMapper, times(1)).save(Mockito.any());
        verify(userMapper, times(1)).findByUserId(Mockito.anyInt());
    }
    @Test
    void createCourseFailed(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(student);
        Mockito.when(courseMapper.save(Mockito.any())).thenReturn(1);
        Response<?> response = userService.createCourse(studentToken, "testCourse", "testCourseDescription", "testBuilding",0);
        assertTrue(!response.isSuccess());
        assertEquals("您没有权限创建课程！", response.getMessage());
    }
    @Test
    void createCourseSuccess(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(teacher);
        Mockito.when(courseMapper.save(Mockito.any())).thenReturn(course);
        Response<?> response = userService.createCourse(teacherToken, "testCourse", "testCourseDescription", "testBuilding",0);
        assertTrue(response.isSuccess());
        assertEquals("创建课程成功", response.getMessage());
        verify(courseMapper, times(1)).save(Mockito.any());
        verify(userMapper, times(1)).findByUserId(Mockito.anyInt());
    }
    @Test
    void deleteCourseRoleError(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(student);
        Response<?> response = userService.deleteCourse(studentToken, "testCourse");
        assertTrue(!response.isSuccess());
        assertEquals("您没有权限删除课程！", response.getMessage());
    }
    @Test
    void deleteCourseNameError(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(teacher);
        Mockito.when(courseMapper.findByCourseName(Mockito.anyString())).thenReturn(null);
        Response<?> response = userService.deleteCourse(teacherToken, "testCourse");
        assertTrue(!response.isSuccess());
        assertEquals("课程不存在！", response.getMessage());
    }
    @Test
    void deleteCourse(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(teacher);
        Mockito.when(courseMapper.findByCourseName(Mockito.anyString())).thenReturn(course);
        Mockito.when(courseMapper.deleteByCourseName(Mockito.anyString())).thenReturn(1);
        Response<?> response = userService.deleteCourse(teacherToken, "testCourse");
        assertTrue(response.isSuccess());
        assertEquals("删除课程成功", response.getMessage());
        verify(courseMapper, times(1)).deleteByCourseName(Mockito.anyString());
    }
    @Test
    void joinCourseRoleError(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(teacher);
        Response<?> response = userService.joinCourse(teacherToken, "testCourse");
        assertTrue(!response.isSuccess());
        assertEquals("您没有权限加入课程！", response.getMessage());
    }
    @Test
    void joinCourseNameError(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(student);
        Mockito.when(courseMapper.findByCourseName(Mockito.anyString())).thenReturn(null);
        Response<?> response = userService.joinCourse(studentToken, "testCourse");
        assertTrue(!response.isSuccess());
        assertEquals("课程不存在！", response.getMessage());
    }
    @Test
    void joinCourseAlreadyError(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(student);
        Mockito.when(courseMapper.findByCourseName(Mockito.anyString())).thenReturn(course);
        CourseSelection courseSelection = new CourseSelection();
        Mockito.when(courseSelectionMapper.findByStudentAndCourse(student,course)).thenReturn(courseSelection);
        Response<?> response = userService.joinCourse(studentToken, "testCourse");
        assertTrue(!response.isSuccess());
        assertEquals("您已经加入该课程！", response.getMessage());
    }
    @Test
    void joinCourse(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(student);
        Mockito.when(courseMapper.findByCourseName(Mockito.anyString())).thenReturn(course);
        Mockito.when(courseSelectionMapper.findByStudentAndCourse(student,course)).thenReturn(null);
        Mockito.when(courseSelectionMapper.save(Mockito.any())).thenReturn(new CourseSelection());
        Response<?> response = userService.joinCourse(studentToken, "testCourse");
        assertTrue(response.isSuccess());
        assertEquals("加入课程成功", response.getMessage());
        verify(courseSelectionMapper, times(1)).save(Mockito.any());
    }
    @Test
    void getCourseStudent(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(student);
        Mockito.when(courseSelectionMapper.findByStudent(student)).thenReturn(courseSelectionList);
        Response<?> response = userService.getCourse(studentToken);
        assertTrue(response.isSuccess());
        assertEquals("获取课程成功", response.getMessage());
        assertEquals(1, ((List<CourseStudentVO>)response.getData()).size());
        verify(courseSelectionMapper, times(1)).findByStudent(Mockito.any());
    }
    @Test
    void getCourseTeacher(){
        // mock 掉 userMapper 的 findById 方法
        Mockito.when(userMapper.findByUserId(Mockito.anyInt())).thenReturn(teacher);
        Mockito.when(courseMapper.findByTeacher(teacher)).thenReturn(courseList);
        Mockito.when(courseSelectionMapper.findByCourse(course)).thenReturn(courseSelectionList);
        Response<?> response = userService.getCourse(teacherToken);
        assertTrue(response.isSuccess());
        assertEquals("获取课程成功", response.getMessage());
        assertEquals(1, ((List<CourseTeacherVO>)response.getData()).size());
        verify(courseMapper, times(1)).findByTeacher(Mockito.any());
    }

}