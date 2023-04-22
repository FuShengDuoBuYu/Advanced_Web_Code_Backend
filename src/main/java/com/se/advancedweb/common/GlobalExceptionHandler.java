package com.se.advancedweb.common;


import com.se.advancedweb.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//处理全局异常
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e){
//        e.printStackTrace();
        Logger LOG = LoggerFactory.getLogger(e.getClass());
        LOG.error(e.getMessage(), e);
        return new Response(false, e.getMessage()!=null?e.getMessage():"服务器发生错误！");
    }
}
