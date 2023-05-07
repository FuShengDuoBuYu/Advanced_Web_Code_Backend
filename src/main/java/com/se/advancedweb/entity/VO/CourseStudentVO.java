package com.se.advancedweb.entity.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseStudentVO {
    private String courseName;
    private String courseDescription;
    private String teacherName;
}
