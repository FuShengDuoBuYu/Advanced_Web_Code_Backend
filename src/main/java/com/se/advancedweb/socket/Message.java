package com.se.advancedweb.socket;

import java.util.UUID;

public class Message {
    public String id;
    public String message;

    public Message(String id, String message) {
        this.id = id;
        this.message = message;
    }
}
