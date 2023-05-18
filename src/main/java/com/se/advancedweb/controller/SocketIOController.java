package com.se.advancedweb.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.se.advancedweb.socket.ClientCache;
import com.se.advancedweb.socket.MessageInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Resource;

@Component
@RestController
//@RequestMapping("/push")
public class SocketIOController {
    @Resource
    private ClientCache clientCache;

//    @GetMapping("/user/{userId}")
    public String pushTuUser(@PathVariable("userId") String userId){
        HashMap<UUID, SocketIOClient> userClient = clientCache.getClientsByRoomId(userId);
        userClient.forEach((uuid, socketIOClient) -> {
            //向客户端推送消息
            socketIOClient.sendEvent("chat message",new MessageInfo("管理员","向客户段发送的消息"));
        });
        return "success";
    }
}
