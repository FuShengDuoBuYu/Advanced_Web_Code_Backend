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
@Table(appliesTo = "course", comment = "课程")
@GenericGenerator(name ="increment", strategy = "increment")

public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id", nullable = false)
    private int courseId;

    @Column(name = "course_name", length = 256, nullable = false)
    private String courseName;

    @Column(name = "course_description", length = 256, nullable = false)
    private String courseDescription;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User teacher;

    public Course(String courseName, String courseDescription,User teacher) {
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.teacher = teacher;
    }


}
