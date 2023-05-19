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
            blockInfoMap.put(roomId, blockInfos);
        }
        blockInfos.add(blockInfo);
        System.out.println(blockInfos);
    }

    public List<BlockInfo> getBlockInfo(String roomId){
        return blockInfoMap.get(roomId);
    }

    public void deleteBlockInfo(String roomId, BlockInfo blockInfo){
        List<BlockInfo> list = blockInfoMap.get(roomId);
        System.out.println("before" + list);
        for (int i = 0; i < list.size(); i++) {
            BlockInfo blockInfo1 = list.get(i);
            // float类型比较大小
            if (Math.abs(blockInfo1.x1 - blockInfo.x1) < 0.0001
                    && Math.abs(blockInfo1.y1 - blockInfo.y1) < 0.0001
                    && Math.abs(blockInfo1.z1 - blockInfo.z1) < 0.0001 ) {
                list.remove(i);
                break;
            }
        }
        System.out.println("after" + list);
        //更新blockInfomap的值】
        blockInfoMap.put(roomId, list);
    }
}
