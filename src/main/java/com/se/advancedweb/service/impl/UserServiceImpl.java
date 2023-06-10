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
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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
        // 获取总的学习时长
        List<UserConnectDuration> userConnectDurationList = userConnectDurationMapper.findByUser(user);
        int totalDuration = 0;
        for(UserConnectDuration userConnectDuration : userConnectDurationList){
            totalDuration += userConnectDuration.getDuration();
        }
        // 获取发言总次数
        List<UserChatMessage> userChatMessageList = userChatMessageMapper.findByUser(user);
        int totalChatTimes = userChatMessageList.size();
        // 获取上次登陆时间
        List<UserLoginHistory> userLoginHistoryList = userLoginHistoryMapper.findByUser(user);
        // 转换为字符串
        String lastLoginTimeStr = "无登陆记录";
        if (userLoginHistoryList.size()>0){
            Timestamp lastLoginTime = userLoginHistoryList.get(userLoginHistoryList.size() - 1).getTime();
            lastLoginTimeStr = lastLoginTime.toString();
        }
        // 获取上次发言内容
        String lastChatMessage = "无发言记录";
        if (userChatMessageList.size()>0){
            userChatMessageList.get(userChatMessageList.size() - 1).getMessage();
        }

        //更新token
        String newToken = TokenUtil.getToken(id, user.getUsername(), String.valueOf(user.getRole()), user.getPassword());
        data.put("token", newToken);
        data.put("user_name", user.getUsername());
        data.put("role", user.getRole());
        data.put("last_login_time", lastLoginTimeStr);
        data.put("total_duration", totalDuration);
        data.put("total_chat_times", totalChatTimes);
        data.put("last_chat_message", lastChatMessage);
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
        else {
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
        HashMap<String, Long> map = new HashMap<>();
        List<User> users= userMapper.findAll();
        for(User user : users){
            List<UserConnectDuration> userConnectDurations = userConnectDurationMapper.findByUser(user);
            Long totalDuration = 0L;
            for (UserConnectDuration userConnectDuration : userConnectDurations){
                totalDuration += userConnectDuration.getDuration();
            }
            map.put(user.getUsername(), totalDuration);
        }
        return new Response<>(true, "获取所有用户连接时长成功(单位：秒）", map);
    }

    @Override
    public Response<?> getSevenDaysDuration(String token){
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findByUserId(Integer.parseInt(id));
        List<UserConnectDuration> userConnectDurations = userConnectDurationMapper.findByUser(user);
        HashMap<Integer, Integer> map = new HashMap<>();
        map.put(6, 0);
        map.put(5, 0);
        map.put(4, 0);
        map.put(3, 0);
        map.put(2, 0);
        map.put(1, 0);
        map.put(0, 0);
        // 当天的日期
        Timestamp today = new Timestamp(System.currentTimeMillis());
        for (UserConnectDuration userConnectDuration : userConnectDurations){
            Timestamp timestamp = userConnectDuration.getTime();
            if (timestamp == null){
                continue;
            }
            // 两个时间相差的天数
            int days = (int) ((today.getTime() - timestamp.getTime()) / (1000*3600*24));
            if (days <= 6){
                map.put(days, map.get(days) + userConnectDuration.getDuration().intValue());
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lineValue", map);
        return new Response<>(true, "获取用户近七天连接时长成功(单位：秒）", jsonObject);
    }

    @Override
    public Response<?> getCourseChatTimes(String token){
        String id = TokenUtil.getUserId(token);
        User user = userMapper.findByUserId(Integer.parseInt(id));
        List<UserChatMessage> userChatMessages = userChatMessageMapper.findByUser(user);
        HashMap<String, Integer> map = new HashMap<>();
        for (UserChatMessage userChatMessage : userChatMessages){
            Course course = userChatMessage.getCourse();
            if (map.containsKey(course.getCourseName())){
                // 更新值
                map.put(course.getCourseName(), map.get(course.getCourseName()) + 1);
            }
            else {
                map.put(course.getCourseName(), 1);
            }
        }
        return new Response<>(true, "获取用户课程聊天次数成功", map);
    }
}