package com.se.advancedweb.socket;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.se.advancedweb.entity.Course;
import com.se.advancedweb.entity.User;
import com.se.advancedweb.entity.UserChatMessage;
import com.se.advancedweb.entity.UserConnectDuration;
import com.se.advancedweb.mapper.CourseMapper;
import com.se.advancedweb.mapper.UserChatMessageMapper;
import com.se.advancedweb.mapper.UserConnectDurationMapper;
import com.se.advancedweb.mapper.UserMapper;
import com.se.advancedweb.util.MossAPI;
import com.se.advancedweb.util.OpenAIAPI;
import lombok.Data;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Data
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

    private UserConnectDurationMapper userConnectDurationMapper;
    private UserMapper userMapper;
    private CourseMapper courseMapper;
    private UserChatMessageMapper userChatMessageMapper;

    @Autowired
    public SocketIOConfig(UserConnectDurationMapper userConnectDurationMapper, UserMapper userMapper, CourseMapper courseMapper, UserChatMessageMapper userChatMessageMapper){
        this.userConnectDurationMapper = userConnectDurationMapper;
        this.userMapper = userMapper;
        this.userChatMessageMapper = userChatMessageMapper;
        this.courseMapper = courseMapper;
    }

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
        configuration.setMaxFramePayloadLength(1024 * 1024 * 1024);
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
            int roomId = Integer.parseInt(client.getHandshakeData().getSingleUrlParam("roomId"));
            String userName = client.getHandshakeData().getSingleUrlParam("userName");
            // 加入连接时间戳
            client.set("connectTime", Instant.now());
            UUID sessionId = client.getSessionId();
            clientCache.saveClient(roomId, sessionId, client);
            System.out.println(" 用户" + userName + " 加入房间 - " + roomId);

            client.set("userInfo", new UserInfo(client.getSessionId().toString(), "", userName,0,0,0,0));

            List<BlockInfo> blockInfoList = clientCache.getBlockInfo(roomId);
            if (blockInfoList != null) {
                // 初始化block
                for (BlockInfo blockInfo: clientCache.getBlockInfo(roomId)) {
                    client.sendEvent("addBlock", blockInfo);
                }
            }
        });

        server.addDisconnectListener(client -> {
            int roomId = Integer.parseInt(client.getHandshakeData().getSingleUrlParam("roomId"));
            System.out.println("roomId: " + roomId);
            String userName = client.getHandshakeData().getSingleUrlParam("userName");
            // 计算连接时间
            Instant connectTime = client.get("connectTime");
            Instant disconnectTime = Instant.now();
            long duration = disconnectTime.getEpochSecond() - connectTime.getEpochSecond();
            // 保存连接时间
            String realUserName = userName.split("-")[1];
            User user = userMapper.findByUsername(realUserName);
            Course course = courseMapper.findByCourseId(roomId);
            UserConnectDuration userConnectDuration = new UserConnectDuration(duration, course, user);
            userConnectDurationMapper.save(userConnectDuration);

            UUID sessionId = client.getSessionId();
            clientCache.deleteSessionClientByUserId(roomId, sessionId);
            System.out.println("roomId: " + roomId + " 用户" + userName + " 退出房间 - " + roomId);

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
            if (!Objects.equals(data.getString("type"), "image")){
                // 存储聊天记录
                String realUserName = data.getString("userName").split("-")[1];
                User user = userMapper.findByUsername(realUserName);
                Course course = courseMapper.findByCourseId(data.getIntValue("roomId"));
                UserChatMessage userChatMessage = new UserChatMessage(data.getString("message"), course, user);
                userChatMessageMapper.save(userChatMessage);
            }

            HashMap<UUID, SocketIOClient> clients = ClientCache.concurrentHashMap.get(data.getIntValue("roomId"));
            for (Map.Entry<UUID, SocketIOClient> entry: clients.entrySet()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userName", data.getString("userName"));
                jsonObject.put("message", data.getString("message"));
                jsonObject.put("type", data.getString("type"));
                entry.getValue().sendEvent("chat", jsonObject);
            }
        });
        server.addEventListener("AI_assistant", JSONObject.class, (client, data, ackSender) -> {
            // 若client中没有存储moss_context字段，则初始化
            if (client.get("moss_context") == null){
                client.set("moss_context", "");
            }
            System.out.println("socket.AI_assistant message " + data.getString("userName") + data.getString("message"));
            List<Map<String, String>> dataList = data.getObject("dataList", List.class);
            String message = data.getString("message");
            String model = data.getString("model");
            String response = "";
            if (model.equals("gpt3.5")){
                response = OpenAIAPI.chat(message, dataList);
            }
            if (model.equals("moss")){
                String context = client.get("moss_context");
                HashMap<String, String>result = MossAPI.sendRequest(message, context);
                response = result.get("response");
                context = result.get("context");
                client.set("moss_context", context);
            }
            System.out.println("socket.AI_assistant response " + response);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", response);
            client.sendEvent("AI_assistant", jsonObject);
        });


        server.addEventListener("speech", JSONObject.class, (client, data, ackSender) -> {
            System.out.println("socket.speech message " + data.getString("userName"));
            System.out.println("socket.speech message " + client);
//            System.out.println(data.getString("message"));
            HashMap<UUID, SocketIOClient> clients = ClientCache.concurrentHashMap.get(data.getIntValue("roomId"));
            for (Map.Entry<UUID, SocketIOClient> entry: clients.entrySet()){
                if (entry.getValue().equals(client)) {
                    continue;
                }
                System.out.println("socket.speech message " + entry.getValue());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userName", data.getString("userName"));
                jsonObject.put("message", data.getString("message"));
                entry.getValue().sendEvent("speech", jsonObject);
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

        server.addEventListener("addBlock", JSONObject.class, (client, data, ackSender) -> {
            System.out.println("socket.block " + data.toJSONString());
//            System.out.println(ClientCache.blockInfoMap.get(data.getString("roomId")));
            int roomId = data.getIntValue("roomId");
            BlockInfo blockInfo = new BlockInfo(data.getFloatValue("x1"), data.getFloatValue("y1"), data.getFloatValue("z1"),
                    data.getFloatValue("x2"), data.getFloatValue("y2"), data.getFloatValue("z2"));
            clientCache.saveBlockInfo(roomId, blockInfo);

            // 广播添加消息
            for (Map.Entry<UUID, SocketIOClient> entry: clientCache.getClients(roomId).entrySet()){
                entry.getValue().sendEvent("addBlock", data);
            }
        });

        server.addEventListener("deleteBlock", JSONObject.class, (client, data, ackSender) -> {
            System.out.println("socket.deleteBlock " + data.toJSONString());
            int roomId = data.getIntValue("roomId");
            BlockInfo blockInfo = new BlockInfo(data.getFloatValue("x1"), data.getFloatValue("y1"), data.getFloatValue("z1"),
                    data.getFloatValue("x2"), data.getFloatValue("y2"), data.getFloatValue("z2"));
            clientCache.deleteBlockInfo(roomId, blockInfo);

            // 广播删除消息
            for (Map.Entry<UUID, SocketIOClient> entry: clientCache.getClients(roomId).entrySet()){
                entry.getValue().sendEvent("deleteBlock", data);
            }
        });

        // 添加定时任务
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try{
//                System.out.println("定时任务" + server.getAllClients().size());
                // 遍历整个concurrentHashMap，以key为id，value为SocketIOClient
                for (Map.Entry<Integer, HashMap<UUID, SocketIOClient>> entry: ClientCache.concurrentHashMap.entrySet()){
                    List<JSONObject> pack = new ArrayList<>();
                    for (Map.Entry<UUID, SocketIOClient> entry1: entry.getValue().entrySet()){
                        UserInfo userInfo = entry1.getValue().get("userInfo");
                        if (userInfo.rolename == "") continue;
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
