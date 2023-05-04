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

@Table(appliesTo = "user_login_history", comment = "用户登录历史表")
@GenericGenerator(name ="increment", strategy = "increment")
public class UserLoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_login_history_id", nullable = false)
    private int userLoginHistoryId;
    @Column(name = "time", length = 256, nullable = false)
    private Timestamp time;
    @Column(name = "operation", length = 256, nullable = false)
    private String operation;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    public UserLoginHistory(Timestamp time, String operation, User user) {
        this.time = time;
        this.operation = operation;
        this.user = user;
    }
}
