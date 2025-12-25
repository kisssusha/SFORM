package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.EnrollmentResponse;
import org.example.entity.Enrollment;
import org.example.exception.ExistEntityException;
import org.example.exception.InvalidRequestException;
import org.example.mapper.EnrollmentMapper;
import org.example.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    private final EnrollmentMapper enrollmentMapper;

    @PostMapping("/enroll")
    public EnrollmentResponse enrollUserToCourse(
            @RequestParam Long userId,
            @RequestParam Long courseId
    ) {
        if (enrollmentService.isUserAlreadyEnrolled(userId, courseId)) {
            throw new ExistEntityException("User is already enrolled in this course");
        }
        Enrollment enrollment = enrollmentService.enrollUserToCourse(userId, courseId);
        return enrollmentMapper.toResponse(enrollment);
    }

    @PostMapping("/unenroll")
    public ResponseEntity<Void> unenrollUserFromCourse(
            @RequestParam Long userId,
            @RequestParam Long courseId
    ) {
        if (!enrollmentService.isUserAlreadyEnrolled(userId, courseId)) {
            throw new ExistEntityException("User is not enrolled in this course");
        }
        enrollmentService.unenrollUserFromCourse(userId, courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<EnrollmentResponse> getAll() {
        return enrollmentService.getAll()
                .stream()
                .map(enrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EnrollmentResponse getEnrollmentById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Id cannot be null");
        }
        Enrollment enrollment = enrollmentService.getEnrollmentById(id);
        return enrollmentMapper.toResponse(enrollment);
    }
}
