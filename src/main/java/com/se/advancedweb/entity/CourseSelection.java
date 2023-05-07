package com.se.advancedweb.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

@Table(appliesTo = "course_selection", comment = "学生选课")
@GenericGenerator(name ="increment", strategy = "increment")

public class CourseSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_selection_id", nullable = false)
    private int courseSelectionId;
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User student;

    public CourseSelection(Course course, User student) {
        this.course = course;
        this.student = student;
    }


}
