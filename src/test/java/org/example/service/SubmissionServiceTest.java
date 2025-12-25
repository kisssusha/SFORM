package org.example.service;

import org.example.dto.request.SubmissionRequest;
import org.example.entity.Assignment;
import org.example.entity.Submission;
import org.example.entity.User;
import org.example.repository.AssignmentRepository;
import org.example.repository.SubmissionRepository;
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
public class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubmissionService submissionService;

    /**
     * Должен вернуть список всех сдач заданий.
     */
    @Test
    public void shouldReturnAllSubmissionsWhenGetAllIsCalled() {
        // Given
        List<Submission> submissions = Arrays.asList(
                new Submission(),
                new Submission()
        );
        when(submissionRepository.findAll()).thenReturn(submissions);

        // When
        List<Submission> result = submissionService.getAll();

        // Then
        assertThat(result)
                .as("Список сдач должен содержать 2 элемента")
                .hasSize(2);
        verify(submissionRepository).findAll();
    }

    /**
     * Должен вернуть все сдачи по ID задания.
     */
    @Test
    public void shouldReturnSubmissionsByAssignmentIdWhenExist() {
        // Given
        List<Submission> submissions = Arrays.asList(
                new Submission(),
                new Submission()
        );
        when(submissionRepository.findByAssignmentId(1L)).thenReturn(submissions);

        // When
        List<Submission> result = submissionService.getSubmissionsByAssignmentId(1L);

        // Then
        assertThat(result)
                .as("Список сдач по заданию должен содержать 2 элемента")
                .hasSize(2);
        verify(submissionRepository).findByAssignmentId(1L);
    }

    /**
     * Должен вернуть все сдачи студента по его ID.
     */
    @Test
    public void shouldReturnSubmissionsByStudentIdWhenExist() {
        // Given
        List<Submission> submissions = Arrays.asList(
                new Submission(),
                new Submission()
        );
        when(submissionRepository.findByStudentId(1L)).thenReturn(submissions);

        // When
        List<Submission> result = submissionService.getSubmissionsByStudentId(1L);

        // Then
        assertThat(result)
                .as("Список сдач студента должен содержать 2 элемента")
                .hasSize(2);
        verify(submissionRepository).findByStudentId(1L);
    }

    /**
     * Должен успешно сдать задание, если задание и студент существуют, и ещё не сдавались.
     */
    @Test
    public void shouldSubmitAssignmentSuccessfullyWhenNotAlreadySubmitted() {
        // Given
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTitle("Assignment 1: Implement Calculator");

        User student = new User();
        student.setId(2L);
        student.setName("Alice Johnson");

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(submissionRepository.existsByStudentIdAndAssignmentId(2L, 1L)).thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.setSubmittedAt(java.time.LocalDateTime.now());
            return saved;
        });

        // When
        Submission submission = submissionService.submitAssignment(1L, 2L, "Submitted solution for Assignment 1");

        // Then
        assertThat(submission)
                .as("Сдача должна быть создана")
                .isNotNull();
        assertThat(submission.getAssignment().getId())
                .as("Задание должно быть привязано")
                .isEqualTo(1L);
        assertThat(submission.getStudent().getId())
                .as("Студент должен быть привязан")
                .isEqualTo(2L);
        assertThat(submission.getContent())
                .as("Содержание должно совпадать")
                .isEqualTo("Submitted solution for Assignment 1");
        assertThat(submission.getSubmittedAt())
                .as("Дата сдачи должна быть установлена")
                .isNotNull();
        verify(assignmentRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(submissionRepository).existsByStudentIdAndAssignmentId(2L, 1L);
        verify(submissionRepository).save(any(Submission.class));
    }

    /**
     * Должен обновить сдачу, если все данные корректны.
     */
    @Test
    public void shouldUpdateSubmissionWhenValidRequestProvided() {
        // Given
        Submission existing = new Submission();
        existing.setContent("Old: Basic implementation");
        existing.setScore(50);
        existing.setFeedback("Needs better error handling");

        Assignment oldAssignment = new Assignment();
        oldAssignment.setId(1L);
        existing.setAssignment(oldAssignment);

        User oldStudent = new User();
        oldStudent.setId(1L);
        existing.setStudent(oldStudent);

        SubmissionRequest request = new SubmissionRequest();
        request.setContent("Improved version with bug fixes");
        request.setScore(85);
        request.setFeedback("Good job, minor improvements needed");
        request.setAssignmentId(2L);
        request.setStudentId(3L);

        Assignment newAssignment = new Assignment();
        newAssignment.setId(2L);
        User newStudent = new User();
        newStudent.setId(3L);

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(2L)).thenReturn(Optional.of(newAssignment));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newStudent));
        when(submissionRepository.save(existing)).thenReturn(existing);

        // When
        Submission updated = submissionService.updateSubmission(1L, request);

        // Then
        assertThat(updated.getContent())
                .as("Содержание должно быть обновлено")
                .isEqualTo("Improved version with bug fixes");
        assertThat(updated.getScore())
                .as("Балл должен быть обновлён")
                .isEqualTo(85);
        assertThat(updated.getFeedback())
                .as("Обратная связь должна быть обновлена")
                .isEqualTo("Good job, minor improvements needed");
        assertThat(updated.getAssignment().getId())
                .as("Задание должно быть изменено")
                .isEqualTo(2L);
        assertThat(updated.getStudent().getId())
                .as("Студент должен быть изменён")
                .isEqualTo(3L);
        verify(submissionRepository).save(existing);
    }
}
