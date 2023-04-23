package com.se.advancedweb.socket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Component
public class SocketIOHandler {
    @Resource
    private ClientCache clientCache;

    // 客户端连接的时候触发，前端js触发：socket = io.connect("http://localhost:9092");
    @OnConnect
    public void onConnect(SocketIOClient client){
        String roomId = client.getHandshakeData().getSingleUrlParam("roomId");
        String userName = client.getHandshakeData().getSingleUrlParam("userName");
        UUID sessionId = client.getSessionId();
        clientCache.saveClient(roomId, sessionId, client);
        System.out.println("roomId: "+roomId+"用户"+userName+ "加入房间 - " +sessionId);
    }


    // 客户端关闭连接时触发：前端js触发：socket.disconnect();
    @OnDisconnect
    public void onDisconnect(SocketIOClient client){
        String roomId = client.getHandshakeData().getSingleUrlParam("roomId");
        String userName = client.getHandshakeData().getSingleUrlParam("userName");
        UUID sessionId = client.getSessionId();
        clientCache.deleteSessionClientByUserId(roomId,sessionId);
        System.out.println("roomId: "+roomId+"用户"+userName+"退出房间 - "+sessionId);
    }

    // 自定义消息事件，客户端js触发:socket.emit('chatevent', {msgContent: msg});时触发该方法
    // 前端js的 socket.emit("事件名","参数数据")方法，是触发后端自定义消息事件的时候使用的
    // 前端js的 socket.on("事件名",匿名函数(服务器向客户端发送的数据))为监听服务器端的事件
    @OnEvent("chat message")
    public void chatEvent(SocketIOClient client, AckRequest ackRequest, MessageInfo message){
        HashMap<UUID, SocketIOClient> userClient = clientCache.getUserClient(message.getRoomId());
        System.out.println("userClient: "+userClient);
        Iterator<Map.Entry<UUID, SocketIOClient>> iterator = userClient.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<UUID, SocketIOClient> next = iterator.next();
            next.getValue().sendEvent("chat message", message);
        }
        System.out.println(message);
    }
}
