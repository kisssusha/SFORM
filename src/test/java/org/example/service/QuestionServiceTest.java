package org.example.service;

import org.example.dto.request.QuestionRequest;
import org.example.entity.Question;
import org.example.entity.Quiz;
import org.example.repository.QuestionRepository;
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
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private QuestionService questionService;

    /**
     * Должен вернуть список всех вопросов при вызове getAll.
     */
    @Test
    public void shouldReturnAllQuestionsWhenGetAllIsCalled() {
        // Given
        List<Question> questions = Arrays.asList(
                new Question(),
                new Question()
        );
        when(questionRepository.findAll()).thenReturn(questions);

        // When
        List<Question> result = questionService.getAll();

        // Then
        assertThat(result)
                .as("Список вопросов должен содержать 2 элемента")
                .hasSize(2);
        verify(questionRepository).findAll();
    }

    /**
     * Должен создать вопрос, если викторина существует.
     */
    @Test
    public void shouldCreateQuestionWhenQuizExists() {
        // Given
        Quiz quiz = new Quiz();
        quiz.setId(1L);
        quiz.setTitle("Java Basics Quiz");

        Question question = new Question();
        question.setText("Question 2: Which keyword is used for inheritance?");
        question.setType(Question.QuestionType.SINGLE_CHOICE);
        question.setQuiz(quiz);

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        Question created = questionService.createQuestion(question);

        // Then
        assertThat(created)
                .as("Созданный вопрос не должен быть null")
                .isNotNull();
        assertThat(created.getId())
                .as("ID должен быть присвоен")
                .isEqualTo(100L);
        assertThat(created.getText())
                .as("Текст вопроса должен совпадать")
                .isEqualTo("Question 2: Which keyword is used for inheritance?");
        assertThat(created.getQuiz().getId())
                .as("Викторина должна быть привязана")
                .isEqualTo(1L);
        verify(quizRepository).findById(1L);
        verify(questionRepository).save(question);
    }

    /**
     * Должен обновить вопрос, если все данные корректны.
     */
    @Test
    public void shouldUpdateQuestionWhenValidRequestProvided() {
        // Given
        Question existing = new Question();
        existing.setText("Old: What is OOP?");
        existing.setType(Question.QuestionType.MULTIPLE_CHOICE);

        Quiz oldQuiz = new Quiz();
        oldQuiz.setId(1L);
        existing.setQuiz(oldQuiz);

        QuestionRequest request = new QuestionRequest();
        request.setText("Updated: Question 3: What is polymorphism?");
        request.setType("SINGLE_CHOICE");
        request.setQuizId(2L);

        Quiz newQuiz = new Quiz();
        newQuiz.setId(2L);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(quizRepository.findById(2L)).thenReturn(Optional.of(newQuiz));
        when(questionRepository.save(existing)).thenReturn(existing);

        // When
        Question updated = questionService.updateQuestion(1L, request);

        // Then
        assertThat(updated.getText())
                .as("Текст вопроса должен быть обновлён")
                .isEqualTo("Updated: Question 3: What is polymorphism?");
        assertThat(updated.getType())
                .as("Тип вопроса должен быть изменён на SINGLE_CHOICE")
                .isEqualTo(Question.QuestionType.SINGLE_CHOICE);
        assertThat(updated.getQuiz().getId())
                .as("Викторина должна быть изменена")
                .isEqualTo(2L);
        verify(questionRepository).save(existing);
    }
}
