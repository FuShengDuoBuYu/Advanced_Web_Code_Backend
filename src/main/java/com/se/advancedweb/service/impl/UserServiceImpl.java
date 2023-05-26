package com.se.advancedweb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.se.advancedweb.entity.*;
import com.se.advancedweb.entity.VO.CourseStudentVO;
import com.se.advancedweb.entity.VO.CourseTeacherVO;
import com.se.advancedweb.mapper.*;
import com.se.advancedweb.service.UserService;
import com.se.advancedweb.util.ConstVariable;
import com.se.advancedweb.util.Response;
import com.se.advancedweb.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private UserMapper userMapper;
    private UserLoginHistoryMapper userLoginHistoryMapper;
    private CourseMapper courseMapper;
    private CourseSelectionMapper courseSelectionMapper;
    private UserChatMessageMapper userChatMessageMapper;
    private UserConnectDurationMapper userConnectDurationMapper;
    @Autowired
    public UserServiceImpl(UserMapper userMapper, UserLoginHistoryMapper userLoginHistoryMapper, CourseMapper courseMapper, CourseSelectionMapper courseSelectionMapper, UserChatMessageMapper userChatMessageMapper, UserConnectDurationMapper userConnectDurationMapper) {
        this.userMapper = userMapper;
        this.userLoginHistoryMapper = userLoginHistoryMapper;
        this.courseMapper = courseMapper;
        this.courseSelectionMapper = courseSelectionMapper;
        this.userChatMessageMapper = userChatMessageMapper;
        this.userConnectDurationMapper = userConnectDurationMapper;
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
    public Response<?> getAllUser() {
        return new Response<>(true, "获取所有用户成功！", userMapper.findAll());
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

    @Override
    public Response<?> createCourse(String token, String courseName, String courseDescription, String building, int isOver){
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findByUserId(Integer.parseInt(id));
        if (user.getRole() != ConstVariable.TEACHER){
            return new Response<>(false, "您没有权限创建课程！");
        }

        Course course = new Course(courseName, courseDescription, user, building, isOver);
        courseMapper.save(course);
        return new Response<>(true, "创建课程成功");
    }

    @Override
    public Response<?> getCourseByBuilding(String building) {
        return new Response<>(true, "获取所有课程成功！", courseMapper.findByBuilding(building));
    }

    @Override
    public Response<?> deleteCourse(String token, String courseName){
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findByUserId(Integer.parseInt(id));
        if (user.getRole() != ConstVariable.TEACHER){
            return new Response<>(false, "您没有权限删除课程！");
        }
        Course course = courseMapper.findByCourseName(courseName);
        if (course == null){
            return new Response<>(false, "课程不存在！");
        }
        courseMapper.deleteByCourseName(courseName);
        return new Response<>(true, "删除课程成功");
    }

    @Override
    public Response<?> joinCourse(String token, String courseName){
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findByUserId(Integer.parseInt(id));
        if (user.getRole() != ConstVariable.STUDENT){
            return new Response<>(false, "您没有权限加入课程！");
        }
        Course course = courseMapper.findByCourseName(courseName);
        if (course == null){
            return new Response<>(false, "课程不存在！");
        }

        CourseSelection courseSelection = courseSelectionMapper.findByStudentAndCourse(user, course);
        if (courseSelection != null){
            return new Response<>(false, "您已经加入该课程！");
        }
        CourseSelection newcourseSelection = new CourseSelection(course, user);
        courseSelectionMapper.save(newcourseSelection);
        return new Response<>(true, "加入课程成功");
    }

    @Override
    public Response<?> getCourse(String token){
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findByUserId(Integer.parseInt(id));
        if (user.getRole() == ConstVariable.STUDENT) {
            List<CourseSelection> courseSelections = courseSelectionMapper.findByStudent(user);
            List<CourseStudentVO> courseStudentVOS = new ArrayList<>();
            for (CourseSelection courseSelection : courseSelections){
                Course course = courseSelection.getCourse();
                courseStudentVOS.add(new CourseStudentVO(course.getCourseName(), course.getCourseDescription(), course.getTeacher().getUsername()));
            }
            return new Response<>(true, "获取课程成功", courseStudentVOS);
        }
        else if (user.getRole() == ConstVariable.TEACHER){
            List<Course> courses = courseMapper.findByTeacher(user);
            List<CourseTeacherVO> courseTeacherVOS = new ArrayList<>();
            for (Course course : courses){
                List<String> students = new ArrayList<>();
                List<CourseSelection> courseSelections = courseSelectionMapper.findByCourse(course);
                for (CourseSelection courseSelection : courseSelections){
                    students.add(courseSelection.getStudent().getUsername());
                }
                courseTeacherVOS.add(new CourseTeacherVO(course.getCourseName(), course.getCourseDescription(), students));
            }
            return new Response<>(true, "获取课程成功", courseTeacherVOS);
        }
        else {
            return new Response<>(false, "获取课程失败");
        }
    }
    @Override
    public Response<?> getConnectDuration(String token){
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findByUserId(Integer.parseInt(id));
        List<UserConnectDuration> userConnectDurations = userConnectDurationMapper.findByUser(user);
        HashMap<String, Long> map = new HashMap<>();
        for (UserConnectDuration userConnectDuration : userConnectDurations){
            Course course = userConnectDuration.getCourse();
            if (map.containsKey(course.getCourseName())){
                // 更新值
                map.put(course.getCourseName(), map.get(course.getCourseName()) + userConnectDuration.getDuration());
            }
            else {
                map.put(course.getCourseName(), userConnectDuration.getDuration());
            }
        }
        return new Response<>(true, "获取用户连接时长成功(单位：秒）", map);
    }
    @Override
    public Response<?> getAllConnectDuration(){
        HashMap<String, Integer> map = new HashMap<>();
        List<User> users= userMapper.findAll();
        for(User user : users){
            List<UserConnectDuration> userConnectDurations = userConnectDurationMapper.findByUser(user);
            int totalDuration = 0;
            for (UserConnectDuration userConnectDuration : userConnectDurations){
                totalDuration += userConnectDuration.getDuration();
            }
            map.put(user.getUsername(), totalDuration);
        }
        return new Response<>(true, "获取所有用户连接时长成功(单位：秒）", map);
    }
}