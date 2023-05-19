package com.se.advancedweb.socket;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClientCache {
    // 用户信息缓存
    public static Map<String, HashMap<UUID, SocketIOClient>> concurrentHashMap = new ConcurrentHashMap<>();
    public static Map<String, List<BlockInfo>> blockInfoMap = new ConcurrentHashMap<>();

    //roomId-房间ID | sessionId-页面sessionId | socketIOClient-页面对应的通道连接
    public void saveClient(String roomId,UUID sessionId,SocketIOClient socketIOClient){
        HashMap<UUID, SocketIOClient> sessionIdClientCache = concurrentHashMap.get(roomId);
        if(sessionIdClientCache == null){
            sessionIdClientCache = new HashMap<>();
        }
        sessionIdClientCache.put(sessionId,socketIOClient);
        concurrentHashMap.put(roomId,sessionIdClientCache);
    }

    public HashMap<UUID, SocketIOClient> getClients(String roomId){
        return concurrentHashMap.get(roomId);
    }

    public HashMap<UUID,SocketIOClient> getClientsByRoomId(String roomId){
        return concurrentHashMap.get(roomId);
    }

    public void deleteSessionClientByUserId(String userId,UUID sessionId){
        concurrentHashMap.get(userId).remove(sessionId);
    }

    public void saveBlockInfo(String roomId, BlockInfo blockInfo){
        List<BlockInfo> blockInfos = blockInfoMap.get(roomId);
        if(blockInfos == null){
            blockInfos = new ArrayList<>();
        }
        blockInfos.add(blockInfo);
    }

    public List<BlockInfo> getBlockInfo(String roomId){
        return blockInfoMap.get(roomId);
    }

    public void deleteBlockInfo(String roomId, BlockInfo blockInfo){
        blockInfoMap.get(roomId).remove(blockInfo);
    }
}
