package org.example.service;

import org.example.dto.request.AssignmentRequest;
import org.example.entity.Assignment;
import org.example.entity.Lesson;
import org.example.repository.AssignmentRepository;
import org.example.repository.LessonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    /**
     * Должен вернуть список всех заданий при вызове getAll.
     */
    @Test
    public void shouldReturnAllAssignmentsWhenGetAllIsCalled() {
        // Given
        List<Assignment> assignments = Arrays.asList(
                new Assignment(),
                new Assignment()
        );
        when(assignmentRepository.findAll()).thenReturn(assignments);

        // When
        List<Assignment> result = assignmentService.getAll();

        // Then
        assertThat(result).hasSize(2);
        verify(assignmentRepository, times(1)).findAll();
    }

    /**
     * Должен вернуть задание по ID, если оно существует.
     */
    @Test
    public void shouldReturnAssignmentByIdWhenExists() {
        // Given
        Assignment assignment = new Assignment();
        assignment.setTitle("Debug the Login Module");
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        // When
        Assignment found = assignmentService.getAssignmentById(1L);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Debug the Login Module");
        verify(assignmentRepository).findById(1L);
    }

    /**
     * Должен создать задание, если урок существует.
     */
    @Test
    public void shouldCreateAssignmentWhenLessonExists() {
        // Given
        Lesson lesson = new Lesson();
        lesson.setId(1L);

        Assignment assignment = new Assignment();
        assignment.setTitle("Implement User Registration");
        assignment.setDescription("Create a registration form with validation.");
        assignment.setDueDate(LocalDate.of(2025, 11, 15));
        assignment.setMaxScore(100);
        assignment.setLesson(lesson);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        Assignment created = assignmentService.createAssignment(assignment);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getTitle()).isEqualTo("Implement User Registration");
        assertThat(created.getLesson().getId()).isEqualTo(1L);
        verify(lessonRepository).findById(1L);
        verify(assignmentRepository).save(assignment);
    }

    /**
     * Должен обновить все поля задания при корректном запросе.
     */
    @Test
    public void shouldUpdateAssignmentWhenValidRequestProvided() {
        // Given
        Assignment existing = new Assignment();
        existing.setTitle("Old Assignment");
        existing.setDescription("Outdated task description.");
        existing.setDueDate(LocalDate.of(2025, 1, 1));
        existing.setMaxScore(50);

        Lesson oldLesson = new Lesson();
        oldLesson.setId(1L);
        existing.setLesson(oldLesson);

        AssignmentRequest request = new AssignmentRequest();
        request.setTitle("Updated: Add OAuth Support");
        request.setDescription("Integrate Google and GitHub login.");
        request.setDueDate(LocalDate.of(2025, 12, 10));
        request.setMaxScore(150);
        request.setLessonId(2L);

        Lesson newLesson = new Lesson();
        newLesson.setId(2L);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(lessonRepository.findById(2L)).thenReturn(Optional.of(newLesson));
        when(assignmentRepository.save(existing)).thenReturn(existing);

        // When
        Assignment updated = assignmentService.updateAssignment(1L, request);

        // Then
        assertThat(updated.getTitle()).isEqualTo("Updated: Add OAuth Support");
        assertThat(updated.getDescription()).isEqualTo("Integrate Google and GitHub login.");
        assertThat(updated.getDueDate()).isEqualTo(LocalDate.of(2025, 12, 10));
        assertThat(updated.getMaxScore()).isEqualTo(150);
        assertThat(updated.getLesson().getId()).isEqualTo(2L);
        verify(assignmentRepository).save(existing);
    }
}
