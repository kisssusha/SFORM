package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CourseReviewRequest;
import org.example.entity.Course;
import org.example.entity.CourseReview;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.repository.CourseRepository;
import org.example.repository.CourseReviewRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseReviewService {

    private static final Logger log = LoggerFactory.getLogger(CourseReviewService.class);

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public List<CourseReview> getAll() {
        List<CourseReview> reviews = courseReviewRepository.findAll();
        log.debug("Fetched {} course review(s)", reviews.size());
        return reviews;
    }

    public CourseReview getCourseReviewById(Long id) {
        CourseReview review = courseReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("CourseReview not found: ID=%d", id)
                ));
        log.debug("Fetched CourseReview: ID={}, Rating={}, StudentID={}", id, review.getRating(), review.getStudent().getId());
        return review;
    }

    public CourseReview createCourseReview(CourseReview courseReview) {
        Long courseId = courseReview.getCourse().getId();
        Long studentId = courseReview.getStudent().getId();

        return createCourseReviewByCourseAndStudent(courseReview, courseId, studentId);
    }

    public CourseReview createCourseReview(Long courseId, Long studentId, CourseReview courseReviewDetails) {
        return createCourseReviewByCourseAndStudent(courseReviewDetails, courseId, studentId);
    }

    private CourseReview createCourseReviewByCourseAndStudent(CourseReview courseReview, Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Course not found: ID=%d", courseId)
                ));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found: ID=%d", studentId)
                ));

        courseReview.setCourse(course);
        courseReview.setStudent(student);

        CourseReview saved = courseReviewRepository.save(courseReview);

        log.info("Created CourseReview: ID={}, Rating={}, CourseID={}, StudentID={}",
                saved.getId(), saved.getRating(), courseId, studentId);
        return saved;
    }

    public CourseReview updateCourseReview(Long id, CourseReviewRequest request) {
        CourseReview review = courseReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("CourseReview not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getRating() != null && !request.getRating().equals(review.getRating())) {
            review.setRating(request.getRating());
            log.debug("Updated rating for CourseReview ID={}: {}", id, request.getRating());
            updated = true;
        }

        if (request.getComment() != null && !request.getComment().equals(review.getComment())) {
            review.setComment(request.getComment());
            log.debug("Updated comment for CourseReview ID={}", id);
            updated = true;
        }

        if (request.getStudentId() != null && !request.getStudentId().equals(review.getStudent().getId())) {
            User student = userRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("User not found: ID=%d", request.getStudentId())
                    ));
            review.setStudent(student);
            log.debug("Updated student for CourseReview ID={}: StudentID={}", id, request.getStudentId());
            updated = true;
        }

        if (request.getCourseId() != null && !request.getCourseId().equals(review.getCourse().getId())) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Course not found: ID=%d", request.getCourseId())
                    ));
            review.setCourse(course);
            log.debug("Updated course for CourseReview ID={}: CourseID={}", id, request.getCourseId());
            updated = true;
        }

        if (!updated) {
            log.debug("No changes detected for CourseReview: ID={}", id);
            return review;
        }

        CourseReview saved = courseReviewRepository.save(review);
        log.info("Successfully updated CourseReview: ID={}", id);
        return saved;
    }

    public void deleteCourseReview(Long id) {
        CourseReview review = courseReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("CourseReview not found: ID=%d", id)
                ));

        courseReviewRepository.delete(review);
        log.info("Deleted CourseReview: ID={}, Rating={}, CourseID={}, StudentID={}",
                id, review.getRating(), review.getCourse().getId(), review.getStudent().getId());
    }
}
