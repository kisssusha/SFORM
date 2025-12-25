package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CourseRequest;
import org.example.dto.response.CourseResponse;
import org.example.dto.response.UserResponse;
import org.example.entity.Course;
import org.example.entity.User;
import org.example.exception.InvalidRequestException;
import org.example.mapper.CourseMapper;
import org.example.mapper.UserMapper;
import org.example.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @PostMapping
    public CourseResponse createCourse(
            @RequestBody CourseRequest courseRequest
    ) {
        if (courseRequest == null) {
            throw new InvalidRequestException("CourseRequest cannot be null");
        }
        Course entity = courseMapper.toEntity(courseRequest);
        Course course = courseService.createCourse(entity);
        return courseMapper.toResponse(course);
    }

    @GetMapping("/{id}")
    public CourseResponse getCourseById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Course ID cannot be null");
        }
        Course course = courseService.getCourseById(id);
        return courseMapper.toResponse(course);
    }

    @GetMapping
    public List<CourseResponse> getAllCourses() {
        return courseService.getAll()
                .stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}")
    public List<CourseResponse> getCoursesByUserId(
            @PathVariable Long userId
    ) {
        if (userId == null) {
            throw new InvalidRequestException("User ID cannot be null");
        }
        List<Course> courses = courseService.getCoursesByUserId(userId);
        return courses
                .stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{courseId}/students")
    public List<UserResponse> getStudentsByCourseId(
            @PathVariable Long courseId
    ) {
        if (courseId == null) {
            throw new InvalidRequestException("Course ID cannot be null");
        }
        List<User> students = courseService.getStudentsByCourseId(courseId);
        return students
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public CourseResponse updateCourse(
            @PathVariable Long id,
            @RequestBody CourseRequest courseRequest
    ) {
        if (id == null || courseRequest == null) {
            throw new InvalidRequestException("ID and CourseRequest cannot be null");
        }
        Course updated = courseService.updateCourse(id, courseRequest);
        return courseMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Course ID cannot be null");
        }
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
