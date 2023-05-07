package com.se.advancedweb.mapper;


import com.se.advancedweb.entity.Course;
import com.se.advancedweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMapper extends JpaRepository<Course, Long> {
    Course findByCourseId(int id);
    Course findByCourseName(String name);

    List<Course> findByTeacher(User teacher);
}
