package com.se.advancedweb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(appliesTo = "user_chat_message", comment = "用户聊天消息表")
@GenericGenerator(name ="increment", strategy = "increment")

public class UserChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_chat_message_id", nullable = false)
    private int userChatMessageId;
    @Column(name = "message", length = 256, nullable = false)
    private String message;
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    public UserChatMessage(String message, Course course, User user) {
        this.message = message;
        this.course = course;
        this.user = user;
    }
}
