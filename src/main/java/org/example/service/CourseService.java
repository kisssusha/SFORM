package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CourseRequest;
import org.example.entity.Category;
import org.example.entity.Course;
import org.example.entity.Enrollment;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.exception.InvalidRoleException;
import org.example.repository.CategoryRepository;
import org.example.repository.CourseRepository;
import org.example.repository.EnrollmentRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<Course> getAll() {
        List<Course> courses = courseRepository.findAll();
        log.debug("Fetched {} course(s)", courses.size());
        return courses;
    }

    public Course getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Course not found: ID=%d", id)
                ));
        log.debug("Fetched Course: ID={}, Title='{}'", id, course.getTitle());
        return course;
    }

    public List<Course> getCoursesByUserId(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        List<Course> courses = enrollments.stream()
                .map(Enrollment::getCourse)
                .collect(Collectors.toList());

        log.debug("Fetched {} course(s) for User ID={}", courses.size(), userId);
        return courses;
    }

    public List<User> getStudentsByCourseId(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<User> students = enrollments.stream()
                .map(Enrollment::getUser)
                .collect(Collectors.toList());

        log.debug("Fetched {} student(s) for Course ID={}", students.size(), courseId);
        return students;
    }

    public Course createCourse(Course course) {
        Long teacherId = course.getTeacher().getId();
        Long categoryId = course.getCategory().getId();

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found: ID=%d", teacherId)
                ));

        if (!teacher.getRole().equals(User.Role.TEACHER)) {
            log.warn("User ID={} attempted to create a course but does not have TEACHER role", teacherId);
            throw new InvalidRoleException("Only users with the TEACHER role can lead courses.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Category not found: ID=%d", categoryId)
                ));

        course.setTeacher(teacher);
        course.setCategory(category);

        Course saved = courseRepository.save(course);

        log.info("Created Course: ID={}, Title='{}', TeacherID={}, CategoryID={}",
                saved.getId(), saved.getTitle(), teacherId, categoryId);
        return saved;
    }

    public Course updateCourse(Long id, CourseRequest request) {
        Course course = getCourseById(id);
        boolean updated = false;

        if (request.getTitle() != null && !request.getTitle().equals(course.getTitle())) {
            course.setTitle(request.getTitle());
            log.debug("Updated title for Course ID={}: '{}'", id, request.getTitle());
            updated = true;
        }

        if (request.getDescription() != null && !request.getDescription().equals(course.getDescription())) {
            course.setDescription(request.getDescription());
            log.debug("Updated description for Course ID={}", id);
            updated = true;
        }

        if (request.getTeacherId() != null && !request.getTeacherId().equals(course.getTeacher().getId())) {
            User newTeacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("User not found: ID=%d", request.getTeacherId())
                    ));

            if (!newTeacher.getRole().equals(User.Role.TEACHER)) {
                log.warn("User ID={} is not a TEACHER, cannot assign as teacher for Course ID={}",
                        request.getTeacherId(), id);
                throw new InvalidRoleException("Only users with the TEACHER role can lead courses.");
            }

            course.setTeacher(newTeacher);
            log.debug("Updated teacher for Course ID={}: TeacherID={}", id, request.getTeacherId());
            updated = true;
        }

        if (request.getCategoryId() != null && !request.getCategoryId().equals(course.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Category not found: ID=%d", request.getCategoryId())
                    ));
            course.setCategory(newCategory);
            log.debug("Updated category for Course ID={}: CategoryID={}", id, request.getCategoryId());
            updated = true;
        }

        if (request.getStartDate() != null && !request.getStartDate().equals(course.getStartDate())) {
            course.setStartDate(request.getStartDate());
            log.debug("Updated startDate for Course ID={}: {}", id, request.getStartDate());
            updated = true;
        }

        if (request.getDuration() != null && !request.getDuration().equals(course.getDuration())) {
            course.setDuration(request.getDuration());
            log.debug("Updated duration for Course ID={}: {}", id, request.getDuration());
            updated = true;
        }

        if (!updated) {
            log.debug("No changes detected for Course: ID={}", id);
            return course;
        }

        Course saved = courseRepository.save(course);
        log.info("Successfully updated Course: ID={}, Title='{}'", id, saved.getTitle());
        return saved;
    }

    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courseRepository.delete(course);

        log.info("Deleted Course: ID={}, Title='{}', TeacherID={}, CategoryID={}",
                id, course.getTitle(), course.getTeacher().getId(), course.getCategory().getId());
    }
}
