package com.se.advancedweb.Interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.se.advancedweb.common.VerifyToken;
import com.se.advancedweb.entity.User;
import com.se.advancedweb.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private UserServiceImpl userService;

    @Autowired
    public AuthInterceptor(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从请求头中取出token
        String token = request.getHeader("token");
        //如果不是映射到Controller的方法直接放行
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        //检查需不需要验证token
        if(method.isAnnotationPresent(VerifyToken.class)){
//            VerifyToken loginToken = method.getAnnotation(VerifyToken.class);
//            if(loginToken.required()){
            if(token == null || token == ""){
                throw new RuntimeException("暂无token信息！请先登录获取token！");
            }
            String userId;
            try {
                userId = JWT.decode(token).getAudience().get(0);
            } catch (JWTDecodeException j){
                throw new RuntimeException("token非法！请重新登录！");
            }
            if(JWT.decode(token).getExpiresAt().compareTo(new Date())<0){
                throw new RuntimeException("token已过期！请重新登录！");
            }
            String password = findUserPassword(userId);
            if(password.equals("")){
                throw new RuntimeException("用户不存在！请重新登录！");
            }
            //验证token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(password)).build();
            try {
                jwtVerifier.verify(token);
            } catch (JWTVerificationException e){
                throw new RuntimeException("token验证失败！请重新登录！");
            }
//            }
        }
        return true;
    }

    private String findUserPassword(String userId) {
        String res = "";
        User user = userService.findUserById(userId);
        if (user != null) res = user.getPassword();
        else res = "";
        return res;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
