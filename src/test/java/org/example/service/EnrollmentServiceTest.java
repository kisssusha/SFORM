package org.example.service;

import org.example.entity.Enrollment;
import org.example.repository.CourseRepository;
import org.example.repository.EnrollmentRepository;
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
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    /**
     * Должен вернуть список всех записей на курсы.
     */
    @Test
    public void shouldReturnAllEnrollmentsWhenGetAllIsCalled() {
        // Given
        List<Enrollment> enrollments = Arrays.asList(
                new Enrollment(),
                new Enrollment()
        );
        when(enrollmentRepository.findAll()).thenReturn(enrollments);

        // When
        List<Enrollment> result = enrollmentService.getAll();

        // Then
        assertThat(result).hasSize(2);
        verify(enrollmentRepository).findAll();
    }

    /**
     * Должен удалить запись, если она существует.
     */
    @Test
    public void shouldUnenrollUserWhenEnrollmentExists() {
        // Given
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.of(enrollment));
        doNothing().when(enrollmentRepository).delete(enrollment);

        // When
        enrollmentService.unenrollUserFromCourse(1L, 2L);

        // Then
        verify(enrollmentRepository).findByUserIdAndCourseId(1L, 2L);
        verify(enrollmentRepository).delete(enrollment);
    }
}
