package org.example.service;

import org.example.dto.request.QuizRequest;
import org.example.entity.Module;
import org.example.entity.Quiz;
import org.example.repository.ModuleRepository;
import org.example.repository.QuizRepository;
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
public class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private QuizService quizService;

    /**
     * Должен вернуть список всех викторин при вызове getAll.
     */
    @Test
    public void shouldReturnAllQuizzesWhenGetAllIsCalled() {
        // Given
        List<Quiz> quizzes = Arrays.asList(
                new Quiz(),
                new Quiz()
        );
        when(quizRepository.findAll()).thenReturn(quizzes);

        // When
        List<Quiz> result = quizService.getAll();

        // Then
        assertThat(result)
                .as("Список викторин должен содержать 2 элемента")
                .hasSize(2);
        verify(quizRepository).findAll();
    }

    /**
     * Должен создать викторину, если модуль существует.
     */
    @Test
    public void shouldCreateQuizWhenModuleExists() {
        // Given
        Module module = new Module();
        module.setId(1L);
        module.setTitle("Java Fundamentals");

        Quiz quiz = new Quiz();
        quiz.setTitle("Question 1: What is a class in Java?");
        quiz.setTimeLimit(45);
        quiz.setModule(module);

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> {
            Quiz saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        Quiz created = quizService.createQuiz(quiz);

        // Then
        assertThat(created)
                .as("Созданная викторина не должна быть null")
                .isNotNull();
        assertThat(created.getId())
                .as("ID должен быть присвоен")
                .isEqualTo(100L);
        assertThat(created.getTitle())
                .as("Заголовок должен совпадать")
                .isEqualTo("Question 1: What is a class in Java?");
        assertThat(created.getTimeLimit())
                .as("Лимит времени должен быть 45")
                .isEqualTo(45);
        assertThat(created.getModule().getId())
                .as("Модуль должен быть привязан")
                .isEqualTo(1L);
        verify(moduleRepository).findById(1L);
        verify(quizRepository).save(quiz);
    }

    /**
     * Должен обновить викторину, если все данные корректны.
     */
    @Test
    public void shouldUpdateQuizWhenValidRequestProvided() {
        // Given
        Quiz existing = new Quiz();
        existing.setTitle("Old: Basics of Java");
        existing.setTimeLimit(30);

        Module oldModule = new Module();
        oldModule.setId(1L);
        existing.setModule(oldModule);

        QuizRequest request = new QuizRequest();
        request.setTitle("Updated: Advanced OOP Concepts Quiz");
        request.setTimeLimit(60);
        request.setModuleId(2L);

        Module newModule = new Module();
        newModule.setId(2L);

        when(quizRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(moduleRepository.findById(2L)).thenReturn(Optional.of(newModule));
        when(quizRepository.save(existing)).thenReturn(existing);

        // When
        Quiz updated = quizService.updateQuiz(1L, request);

        // Then
        assertThat(updated.getTitle())
                .as("Заголовок должен быть обновлён")
                .isEqualTo("Updated: Advanced OOP Concepts Quiz");
        assertThat(updated.getTimeLimit())
                .as("Лимит времени должен быть изменён")
                .isEqualTo(60);
        assertThat(updated.getModule().getId())
                .as("Модуль должен быть изменён")
                .isEqualTo(2L);
        verify(quizRepository).save(existing);
    }
}
