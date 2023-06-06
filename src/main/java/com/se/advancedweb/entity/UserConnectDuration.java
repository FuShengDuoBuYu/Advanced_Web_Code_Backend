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
@Table(appliesTo = "user_connect_duration", comment = "用户连接时长表")
@GenericGenerator(name ="increment", strategy = "increment")
public class UserConnectDuration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_connect_duration_id", nullable = false)
    private int userConnectDurationId;
    @Column(name = "duration", length = 256, nullable = false)
    private Long duration;
    @Column(name = "time", length = 256, nullable = true)
    private Timestamp time;
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    public UserConnectDuration(Long duration,Timestamp time ,Course course, User user) {
        this.duration = duration;
        this.time = time;
        this.course = course;
        this.user = user;
    }
}

