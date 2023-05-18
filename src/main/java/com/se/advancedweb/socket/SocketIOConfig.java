package com.se.advancedweb.socket;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class SocketIOConfig implements InitializingBean {
    @Resource
    public ClientCache clientCache;
    @Value("${socketio.port}")
    private Integer port;
    @Value("${socketio.bossCount}")
    private int bossCount;
    @Value("${socketio.workCount}")
    private int workCount;
    @Value("${socketio.allowCustomRequests}")
    private boolean allowCustomRequests;
    @Value("${socketio.upgradeTimeout}")
    private int upgradeTimeout;
    @Value("${socketio.pingTimeout}")
    private int pingTimeout;

    @Value("${socketio.pingInterval}")
    private int pingInterval;


    @Override
    public void afterPropertiesSet() throws Exception {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);

        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        configuration.setSocketConfig(socketConfig);
        // host在本地测试可以设置为localhost或者本机IP，在Linux服务器跑可换成服务器IP
//        configuration.setHostname(host);
        configuration.setPort(port);
        // socket连接数大小（如只监听一个端口boss线程组为1即可）
        configuration.setBossThreads(1);
        configuration.setWorkerThreads(1000);
        configuration.setAllowCustomRequests(allowCustomRequests);
        // 协议升级超时时间（毫秒），默认10秒。HTTP握手升级为ws协议超时时间
        configuration.setUpgradeTimeout(upgradeTimeout);
        // Ping消息超时时间（毫秒），默认60秒，这个时间间隔内没有接收到心跳消息就会发送超时事件
        configuration.setPingTimeout(pingTimeout);
        // Ping消息间隔（毫秒），默认25秒。客户端向服务器发送一条心跳消息间隔
        configuration.setPingInterval(pingInterval);

        SocketIOServer server = new SocketIOServer(configuration);
        //添加事件监听器
        server.addConnectListener(client -> {
            String roomId = client.getHandshakeData().getSingleUrlParam("roomId");
            String userName = client.getHandshakeData().getSingleUrlParam("userName");
            UUID sessionId = client.getSessionId();
            clientCache.saveClient(roomId, sessionId, client);
            System.out.println(" 用户" + userName + " 加入房间 - " + sessionId);

            // 设置 client.userData
            client.set("userData", new UserData(0, 0, 0, 0));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", client.getSessionId().toString());
            client.sendEvent("setId", jsonObject);

            client.set("userInfo", new UserInfo(client.getSessionId().toString(), "", userName,0,0,0,0));
        });

        server.addDisconnectListener(client -> {
            String roomId = client.getHandshakeData().getSingleUrlParam("roomId");
            String userName = client.getHandshakeData().getSingleUrlParam("userName");
            UUID sessionId = client.getSessionId();
            clientCache.deleteSessionClientByUserId(roomId, sessionId);
            System.out.println("roomId: " + roomId + " 用户" + userName + " 退出房间 - " + sessionId);

            // 广播删除消息
            server.getBroadcastOperations().sendEvent("deletePlayer", new JSONObject().put("id", client.getSessionId().toString()));
        });

        server.addEventListener("init", JSONObject.class, (client, data, ackSender) -> {

            UserInfo userInfo = client.get("userInfo");
            System.out.println("socket.init " + userInfo.username);
            userInfo.rolename = data.getString("rolename");
            userInfo.x = data.getFloatValue("x");
            userInfo.y = data.getFloatValue("y");
            userInfo.z = data.getFloatValue("z");
            userInfo.r = data.getFloatValue("r");
        });

        server.addEventListener("chat", JSONObject.class, (client, data, ackSender) -> {
            System.out.println("socket.chat message " + data.getString("userName") + data.getString("message"));
            HashMap<UUID, SocketIOClient> clients = ClientCache.concurrentHashMap.get(data.getString("roomId"));
            for (Map.Entry<UUID, SocketIOClient> entry: clients.entrySet()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userName", data.getString("userName"));
                jsonObject.put("message", data.getString("message"));
                entry.getValue().sendEvent("chat", jsonObject);
            }
        });

        server.addEventListener("move", JSONObject.class, (client, data, ackSender) -> {
//            System.out.println("socket.move " + data.toJSONString());
            UserInfo userInfo = client.get("userInfo");
            userInfo.username = data.getString("username");
            userInfo.rolename = data.getString("rolename");
            userInfo.x = data.getFloatValue("x");
            userInfo.y = data.getFloatValue("y");
            userInfo.z = data.getFloatValue("z");
            userInfo.r = data.getFloatValue("r");
        });

        // 添加定时任务
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try{
//                System.out.println("定时任务" + server.getAllClients().size());
                // 遍历整个concurrentHashMap，以key为id，value为SocketIOClient
                for (Map.Entry<String, HashMap<UUID, SocketIOClient>> entry: ClientCache.concurrentHashMap.entrySet()){
                    List<JSONObject> pack = new ArrayList<>();
                    for (Map.Entry<UUID, SocketIOClient> entry1: entry.getValue().entrySet()){
                        UserInfo userInfo = entry1.getValue().get("userInfo");
                        JSONObject data = new JSONObject();
                        data.put("id", userInfo.id);
                        data.put("username", userInfo.username);
                        data.put("rolename", userInfo.rolename);
                        data.put("x", userInfo.x);
                        data.put("y", userInfo.y);
                        data.put("z", userInfo.z);
                        data.put("r", userInfo.r);
                        pack.add(data);
                    }
                    if (pack.size() > 0) {
                        for (Map.Entry<UUID, SocketIOClient> entry1: entry.getValue().entrySet()){
                            entry1.getValue().sendEvent("remoteData", pack);
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, 0, 40, TimeUnit.MILLISECONDS);
        //启动SocketIOServer
        server.start();
        System.out.println("SocketIO启动完毕");
    }
}
