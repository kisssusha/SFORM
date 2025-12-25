package org.example.service;

import org.example.dto.request.QuizSubmissionRequest;
import org.example.entity.Quiz;
import org.example.entity.QuizSubmission;
import org.example.entity.User;
import org.example.repository.QuizRepository;
import org.example.repository.QuizSubmissionRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizSubmissionServiceTest {

    @Mock
    private QuizSubmissionRepository quizSubmissionRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuizSubmissionService quizSubmissionService;

    /**
     * Должен вернуть список всех попыток прохождения викторин.
     */
    @Test
    public void shouldReturnAllSubmissionsWhenGetAllIsCalled() {
        // Given
        List<QuizSubmission> submissions = Arrays.asList(
                new QuizSubmission(),
                new QuizSubmission()
        );
        when(quizSubmissionRepository.findAll()).thenReturn(submissions);

        // When
        List<QuizSubmission> result = quizSubmissionService.getAll();

        // Then
        assertThat(result)
                .as("Список попыток должен содержать 2 элемента")
                .hasSize(2);
        verify(quizSubmissionRepository).findAll();
    }

    /**
     * Должен вернуть все попытки студента по его ID.
     */
    @Test
    public void shouldReturnSubmissionsByStudentIdWhenExist() {
        // Given
        List<QuizSubmission> submissions = Arrays.asList(
                new QuizSubmission(),
                new QuizSubmission()
        );
        when(quizSubmissionRepository.findByStudentId(1L)).thenReturn(submissions);

        // When
        List<QuizSubmission> result = quizSubmissionService.getSubmissionsByStudentId(1L);

        // Then
        assertThat(result)
                .as("Список попыток студента должен содержать 2 элемента")
                .hasSize(2);
        verify(quizSubmissionRepository).findByStudentId(1L);
    }

    /**
     * Должен вернуть все попытки по ID курса.
     */
    @Test
    public void shouldReturnSubmissionsByCourseIdWhenQuizzesExist() {
        // Given
        Quiz quiz1 = new Quiz();
        quiz1.setId(1L);
        Quiz quiz2 = new Quiz();
        quiz2.setId(2L);
        List<Quiz> quizzes = Arrays.asList(quiz1, quiz2);

        List<QuizSubmission> submissions = Arrays.asList(
                new QuizSubmission(),
                new QuizSubmission()
        );

        when(quizRepository.findByModule_CourseId(1L)).thenReturn(quizzes);
        when(quizSubmissionRepository.findByQuizIdIn(
                quizzes.stream().map(Quiz::getId).collect(Collectors.toList())
        )).thenReturn(submissions);

        // When
        List<QuizSubmission> result = quizSubmissionService.getSubmissionsByCourseId(1L);

        // Then
        assertThat(result)
                .as("Список попыток по курсу должен содержать 2 элемента")
                .hasSize(2);
        verify(quizRepository).findByModule_CourseId(1L);
        verify(quizSubmissionRepository).findByQuizIdIn(Arrays.asList(1L, 2L));
    }

    /**
     * Должен вернуть все попытки по ID модуля.
     */
    @Test
    public void shouldReturnSubmissionsByModuleIdWhenQuizzesExist() {
        // Given
        Quiz quiz1 = new Quiz();
        quiz1.setId(1L);
        Quiz quiz2 = new Quiz();
        quiz2.setId(2L);
        List<Quiz> quizzes = Arrays.asList(quiz1, quiz2);

        List<QuizSubmission> submissions = Arrays.asList(
                new QuizSubmission(),
                new QuizSubmission()
        );

        when(quizRepository.findByModuleId(1L)).thenReturn(quizzes);
        when(quizSubmissionRepository.findByQuizIdIn(
                quizzes.stream().map(Quiz::getId).collect(Collectors.toList())
        )).thenReturn(submissions);

        // When
        List<QuizSubmission> result = quizSubmissionService.getSubmissionsByModuleId(1L);

        // Then
        assertThat(result)
                .as("Список попыток по модулю должен содержать 2 элемента")
                .hasSize(2);
        verify(quizRepository).findByModuleId(1L);
        verify(quizSubmissionRepository).findByQuizIdIn(Arrays.asList(1L, 2L));
    }

    /**
     * Должен успешно создать попытку, если викторина и студент существуют.
     */
    @Test
    public void shouldSubmitQuizSuccessfullyWhenQuizAndStudentExist() {
        // Given
        Quiz quiz = new Quiz();
        quiz.setId(1L);
        quiz.setTitle("Java Basics Quiz");

        User student = new User();
        student.setId(2L);
        student.setName("Alice");

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(quizSubmissionRepository.save(any(QuizSubmission.class))).thenAnswer(invocation -> {
            QuizSubmission saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.setTakenAt(java.time.LocalDateTime.now());
            return saved;
        });

        // When
        QuizSubmission submission = quizSubmissionService.submitQuiz(1L, 2L, 95);

        // Then
        assertThat(submission)
                .as("Попытка должна быть создана")
                .isNotNull();
        assertThat(submission.getQuiz().getId())
                .as("Викторина должна быть привязана")
                .isEqualTo(1L);
        assertThat(submission.getStudent().getId())
                .as("Студент должен быть привязан")
                .isEqualTo(2L);
        assertThat(submission.getScore())
                .as("Балл должен быть 95")
                .isEqualTo(95);
        assertThat(submission.getTakenAt())
                .as("Дата прохождения должна быть установлена")
                .isNotNull();
        verify(quizRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(quizSubmissionRepository).save(any(QuizSubmission.class));
    }

    /**
     * Должен обновить попытку, если все данные корректны.
     */
    @Test
    public void shouldUpdateSubmissionWhenValidRequestProvided() {
        // Given
        QuizSubmission existing = new QuizSubmission();
        existing.setScore(80);

        Quiz oldQuiz = new Quiz();
        oldQuiz.setId(1L);
        existing.setQuiz(oldQuiz);

        User oldStudent = new User();
        oldStudent.setId(1L);
        existing.setStudent(oldStudent);

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setScore(90);
        request.setQuizId(2L);
        request.setStudentId(3L);

        Quiz newQuiz = new Quiz();
        newQuiz.setId(2L);
        User newStudent = new User();
        newStudent.setId(3L);

        when(quizSubmissionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(quizRepository.findById(2L)).thenReturn(Optional.of(newQuiz));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newStudent));
        when(quizSubmissionRepository.save(existing)).thenReturn(existing);

        // When
        QuizSubmission updated = quizSubmissionService.updateQuizSubmission(1L, request);

        // Then
        assertThat(updated.getScore())
                .as("Балл должен быть обновлён")
                .isEqualTo(90);
        assertThat(updated.getQuiz().getId())
                .as("Викторина должна быть изменена")
                .isEqualTo(2L);
        assertThat(updated.getStudent().getId())
                .as("Студент должен быть изменён")
                .isEqualTo(3L);
        verify(quizSubmissionRepository).save(existing);
    }
}
