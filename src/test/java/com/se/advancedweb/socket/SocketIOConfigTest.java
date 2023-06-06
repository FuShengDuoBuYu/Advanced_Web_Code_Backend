package com.se.advancedweb.socket;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.transport.NamespaceClient;
import com.se.advancedweb.mapper.CourseMapper;
import com.se.advancedweb.mapper.UserChatMessageMapper;
import com.se.advancedweb.mapper.UserConnectDurationMapper;
import com.se.advancedweb.mapper.UserMapper;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


import java.net.URISyntaxException;
import java.sql.Time;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



class SocketIOConfigTest {
    private Integer port = 10088;
    private int bossCount = 1;
    private int workCount = 1;
    private boolean allowCustomRequests = true;
    private int upgradeTimeout = 100000;
    private int pingTimeout = 6000000;

    private int pingInterval = 25000;


    private ClientCache clientCacheMock;

    private UserConnectDurationMapper userConnectDurationMapperMock;

    private UserMapper userMapperMock;

    private CourseMapper courseMapperMock;

    private UserChatMessageMapper userChatMessageMapperMock;

    private SocketIOServer socketIOServerMock;

    private SocketIOConfig socketIOConfig;

    @BeforeEach
    void setup() {
        clientCacheMock = mock(ClientCache.class);
        userConnectDurationMapperMock = mock(UserConnectDurationMapper.class);
        userMapperMock = mock(UserMapper.class);
        courseMapperMock = mock(CourseMapper.class);
        userChatMessageMapperMock = mock(UserChatMessageMapper.class);
        socketIOServerMock = mock(SocketIOServer.class);

        socketIOConfig = new SocketIOConfig(
                userConnectDurationMapperMock,
                userMapperMock,
                courseMapperMock,
                userChatMessageMapperMock
        );
        System.out.println("setup");
        socketIOConfig.clientCache = clientCacheMock;
        socketIOConfig.setPort(port);
        socketIOConfig.setBossCount(bossCount);
        socketIOConfig.setAllowCustomRequests(allowCustomRequests);
        socketIOConfig.setUpgradeTimeout(upgradeTimeout);
        socketIOConfig.setPingTimeout(pingTimeout);
        socketIOConfig.setPingInterval(pingInterval);

    }

    @Test
    void connectTest() throws Exception {
        // Mock the SocketIOServer and ConnectListener
        try (MockedConstruction<SocketIOServer> mockedSocketIOServer = Mockito.mockConstruction(SocketIOServer.class, (mock, context) -> {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                    ConnectListener listener = invocation.getArgument(0);
                    SocketIOClient clientMock = Mockito.mock(SocketIOClient.class);
                    when(clientMock.getHandshakeData().getSingleUrlParam("roomId")).thenReturn("123");
                    when(clientMock.getHandshakeData().getSingleUrlParam("userName")).thenReturn("John");
                    when(clientMock.getSessionId()).thenReturn(UUID.randomUUID());
                    when(clientCacheMock.getBlockInfo(anyInt())).thenReturn(null); // Assuming there are no blockInfo for the roomId
                    listener.onConnect(clientMock);
                    return null;
                }
            }).when(mock).addConnectListener(any(ConnectListener.class));
        })) {
            // Invoke the afterPropertiesSet method
            socketIOConfig.afterPropertiesSet();
            // 触发connect事件

            verify(socketIOServerMock).addConnectListener(any(ConnectListener.class));
            verify(clientCacheMock).saveClient(eq(123), any(UUID.class), any(NamespaceClient.class));
            verify(clientCacheMock).getBlockInfo(eq(123));

        }
    }
    @Test
    void test() throws Exception {
        // 启动服务器
        socketIOConfig.afterPropertiesSet();

        // 建立客户端连接
        Socket clientSocket = IO.socket("http://localhost:" + port + "/?roomId=123&userName=John");

        // 监听连接事件
        clientSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connect");
            }
        });
        clientSocket.connect();
        // 发送连接事件
        clientSocket.emit("connect");
//        verify(clientCacheMock).saveClient(eq(123), any(UUID.class), any(SocketIOClient.class));
    }
}
