package com.se.advancedweb.entity;

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
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;
    @Column(name = "username", length = 256, nullable = false)
    private String username;
    @Column(name = "password", length = 256, nullable = false)
    private String password;
    @Column(name = "role", nullable = false)
    private int role;
}