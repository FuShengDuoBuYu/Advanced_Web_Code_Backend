package com.se.advancedweb.entity.VO;

import com.se.advancedweb.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class CourseTeacherVO {
    private String courseName;
    private String courseDescription;
    private List<String> students;
}
