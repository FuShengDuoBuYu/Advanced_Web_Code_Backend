package com.se.advancedweb.socket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageInfo {
    private String roomId;
    private String userName;
    private String message;

    public MessageInfo(String userName, String message) {
        this.userName = userName;
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageInfo{" +
                "roomId='" + roomId + '\'' +
                ", userName='" + userName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
