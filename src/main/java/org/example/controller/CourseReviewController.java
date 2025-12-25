package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CourseReviewRequest;
import org.example.dto.response.CourseReviewResponse;
import org.example.entity.CourseReview;
import org.example.exception.InvalidRequestException;
import org.example.mapper.CourseReviewMapper;
import org.example.service.CourseReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course-reviews")
@RequiredArgsConstructor
public class CourseReviewController {
    private final CourseReviewService courseReviewService;
    private final CourseReviewMapper courseReviewMapper;


    @PostMapping
    public CourseReviewResponse createCourseReview(
            @RequestBody CourseReviewRequest courseReviewRequest
    ) {
        if (courseReviewRequest.getCourseId() == null || courseReviewRequest.getStudentId() == null) {
            throw new InvalidRequestException("Course ID and Student ID are required");
        }
        CourseReview entity = courseReviewMapper.toEntity(courseReviewRequest);
        CourseReview courseReview = courseReviewService.createCourseReview(entity);
        return courseReviewMapper.toResponse(courseReview);
    }

    @GetMapping("/{id}")
    public CourseReviewResponse getCourseReviewById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Course ID is required");
        }
        CourseReview reviewById = courseReviewService.getCourseReviewById(id);
        return courseReviewMapper.toResponse(reviewById);
    }


    @GetMapping
    public List<CourseReviewResponse> getAllReviews() {
        return courseReviewService.getAll()
                .stream()
                .map(courseReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/{courseId}/{studentId}")
    public CourseReviewResponse createCourseReview(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @RequestBody CourseReviewRequest courseReviewRequest) {
        if (courseId == null || studentId == null) {
            throw new InvalidRequestException("Course ID and Student ID are required");
        }
        CourseReview entity = courseReviewMapper.toEntity(courseReviewRequest);
        CourseReview courseReview = courseReviewService.createCourseReview(courseId, studentId, entity);
        return courseReviewMapper.toResponse(courseReview);
    }

    @PutMapping("/{id}")
    public CourseReviewResponse updateCourseReview(
            @PathVariable Long id,
            @RequestBody CourseReviewRequest courseReviewRequest
    ) {
        if (id == null) {
            throw new InvalidRequestException("Course ID is required");
        }
        CourseReview review = courseReviewService.updateCourseReview(id, courseReviewRequest);
        return courseReviewMapper.toResponse(review);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourseReview(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Course ID is required");
        }
        courseReviewService.deleteCourseReview(id);
        return ResponseEntity.noContent().build();
    }
}
