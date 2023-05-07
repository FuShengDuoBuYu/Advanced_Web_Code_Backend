package com.se.advancedweb.mapper;

import com.se.advancedweb.entity.Course;
import com.se.advancedweb.entity.CourseSelection;
import com.se.advancedweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSelectionMapper extends JpaRepository<CourseSelection, Long> {

    List<CourseSelection> findByStudent(User student);
    List<CourseSelection> findByCourse(Course course);

    CourseSelection findByCourseAndStudent(Course course, User student);
    CourseSelection findByStudentAndCourse(User student, Course course);
}
