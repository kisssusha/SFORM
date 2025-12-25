package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.Course;
import org.example.entity.Enrollment;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.repository.CourseRepository;
import org.example.repository.EnrollmentRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public List<Enrollment> getAll() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        log.debug("Fetched {} enrollment(s)", enrollments.size());
        return enrollments;
    }

    public Enrollment getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Enrollment not found: ID=%d", id)
                ));
        log.debug("Fetched Enrollment: ID={}, UserID={}, CourseID={}, Status={}",
                id, enrollment.getUser().getId(), enrollment.getCourse().getId(), enrollment.getStatus());
        return enrollment;
    }

    public boolean isUserAlreadyEnrolled(Long userId, Long courseId) {
        boolean exists = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        if (exists) {
            log.debug("User ID={} is already enrolled in Course ID={}", userId, courseId);
        } else {
            log.debug("User ID={} is not enrolled in Course ID={}", userId, courseId);
        }
        return exists;
    }

    @Transactional
    public Enrollment enrollUserToCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found: ID=%d", userId)
                ));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Course not found: ID=%d", courseId)
                ));

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollDate(LocalDateTime.now());
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);

        Enrollment saved = enrollmentRepository.save(enrollment);

        log.info("User enrolled: UserID={}, CourseID={}, EnrollmentID={}",
                userId, courseId, saved.getId());
        return saved;
    }

    @Transactional
    public void unenrollUserFromCourse(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Enrollment not found for User ID=%d and Course ID=%d", userId, courseId)
                ));

        enrollmentRepository.delete(enrollment);

        log.info("User unenrolled: UserID={}, CourseID={}, EnrollmentID={}",
                userId, courseId, enrollment.getId());
    }
}



