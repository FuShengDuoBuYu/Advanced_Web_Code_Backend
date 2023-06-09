package com.se.advancedweb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Table;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

@Table(appliesTo = "user", comment = "用户表")
@GenericGenerator(name ="increment", strategy = "increment")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private int userId;
    @Column(name = "username", length = 256, nullable = false)
    private String username;
    @JsonIgnore
    @Column(name = "password", length = 256, nullable = false)
    private String password;
    @Column(name = "role", nullable = false)
    private int role;
    @Column(name = "avatar_base64", length = 1024, nullable = false)
    private String avatarBase64;

    public User(String username, String password, int role, String avatarBase64) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.avatarBase64 = avatarBase64;
    }
}