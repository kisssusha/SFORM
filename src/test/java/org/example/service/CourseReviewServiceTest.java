package org.example.service;

import org.example.dto.request.CourseReviewRequest;
import org.example.entity.Course;
import org.example.entity.CourseReview;
import org.example.entity.User;
import org.example.repository.CourseRepository;
import org.example.repository.CourseReviewRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseReviewServiceTest {

    @Mock
    private CourseReviewRepository courseReviewRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseReviewService courseReviewService;

    /**
     * Должен вернуть список всех отзывов по курсам.
     */
    @Test
    public void shouldReturnAllCourseReviewsWhenGetAllIsCalled() {
        // Given
        List<CourseReview> reviews = Arrays.asList(
                new CourseReview(),
                new CourseReview()
        );
        when(courseReviewRepository.findAll()).thenReturn(reviews);

        // When
        List<CourseReview> result = courseReviewService.getAll();

        // Then
        assertThat(result).hasSize(2);
        verify(courseReviewRepository).findAll();
    }

    /**
     * Должен создать отзыв, если курс и студент существуют.
     */
    @Test
    public void shouldCreateReviewWhenCourseAndStudentExist() {
        // Given
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Spring Boot Masterclass");

        User student = new User();
        student.setId(2L);
        student.setName("Alice Johnson");

        CourseReview review = new CourseReview();
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(4);
        review.setComment("Very helpful, but could use more examples.");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseReviewRepository.save(any(CourseReview.class))).thenAnswer(invocation -> {
            CourseReview saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        CourseReview created = courseReviewService.createCourseReview(review);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getRating()).isEqualTo(4);
        assertThat(created.getComment()).isEqualTo("Very helpful, but could use more examples.");
        assertThat(created.getCourse().getId()).isEqualTo(1L);
        assertThat(created.getStudent().getId()).isEqualTo(2L);
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(courseReviewRepository).save(review);
    }

    /**
     * Должен обновить отзыв, если все связанные сущности существуют.
     */
    @Test
    public void shouldUpdateReviewWhenValidRequestProvided() {
        // Given
        CourseReview existing = new CourseReview();
        existing.setRating(3);
        existing.setComment("Average course, nothing special.");

        Course oldCourse = new Course();
        oldCourse.setId(1L);
        existing.setCourse(oldCourse);

        User oldStudent = new User();
        oldStudent.setId(1L);
        existing.setStudent(oldStudent);

        CourseReviewRequest request = new CourseReviewRequest();
        request.setRating(5);
        request.setComment("Actually, it's outstanding after second look!");
        request.setCourseId(2L);
        request.setStudentId(3L);

        Course newCourse = new Course();
        newCourse.setId(2L);

        User newStudent = new User();
        newStudent.setId(3L);

        when(courseReviewRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(newCourse));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newStudent));
        when(courseReviewRepository.save(existing)).thenReturn(existing);

        // When
        CourseReview updated = courseReviewService.updateCourseReview(1L, request);

        // Then
        assertThat(updated.getRating()).isEqualTo(5);
        assertThat(updated.getComment()).isEqualTo("Actually, it's outstanding after second look!");
        assertThat(updated.getCourse().getId()).isEqualTo(2L);
        assertThat(updated.getStudent().getId()).isEqualTo(3L);
        verify(courseReviewRepository).save(existing);
    }
}
