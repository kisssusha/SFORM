package org.example.service;

import org.example.dto.request.AnswerOptionRequest;
import org.example.entity.AnswerOption;
import org.example.entity.Question;
import org.example.exception.EntityNotFoundException;
import org.example.repository.AnswerOptionRepository;
import org.example.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnswerOptionServiceTest {

    @Mock
    private AnswerOptionRepository answerOptionRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private AnswerOptionService answerOptionService;

    /**
     * Должен вернуть список всех вариантов ответов при вызове fetchAllAnswerOptions.
     */
    @Test
    public void shouldReturnAllAnswerOptionsWhenFetchAllIsCalled() {
        // Given
        List<AnswerOption> options = Arrays.asList(
                new AnswerOption(),
                new AnswerOption()
        );
        when(answerOptionRepository.findAll()).thenReturn(options);

        // When
        List<AnswerOption> result = answerOptionService.fetchAllAnswerOptions();

        // Then
        assertThat(result).hasSize(2);
        verify(answerOptionRepository, times(1)).findAll();
    }

    /**
     * Должен вернуть вариант ответа по ID, если он существует.
     */
    @Test
    public void shouldReturnAnswerOptionByIdWhenExists() {
        // Given
        AnswerOption option = new AnswerOption();
        option.setText("Java is a programming language.");
        when(answerOptionRepository.findById(1L)).thenReturn(Optional.of(option));

        // When
        AnswerOption result = answerOptionService.fetchAnswerOptionById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Java is a programming language.");
        verify(answerOptionRepository).findById(1L);
    }

    /**
     * Должен выбросить EntityNotFoundException, если вариант ответа не найден.
     */
    @Test
    public void shouldThrowExceptionWhenAnswerOptionNotFound() {
        // Given
        when(answerOptionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerOptionService.fetchAnswerOptionById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
        verify(answerOptionRepository).findById(1L);
    }

    /**
     * Должен создать вариант ответа, если вопрос существует.
     */
    @Test
    public void shouldCreateAnswerOptionWhenQuestionExists() {
        // Given
        Question question = new Question();
        question.setId(1L);

        AnswerOption option = new AnswerOption();
        option.setText("Yes");
        option.setIsCorrect(true);
        option.setQuestion(question);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(answerOptionRepository.save(any(AnswerOption.class))).thenAnswer(invocation -> {
            AnswerOption saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        AnswerOption created = answerOptionService.createAnswerOption(option);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getText()).isEqualTo("Yes");
        assertThat(created.getIsCorrect()).isTrue();
        assertThat(created.getQuestion().getId()).isEqualTo(1L);
        verify(questionRepository).findById(1L);
        verify(answerOptionRepository).save(option);
    }

    /**
     * Должен обновить текст, статус и вопрос варианта ответа при корректных данных.
     */
    @Test
    public void shouldUpdateAnswerOptionWhenValidRequestProvided() {
        // Given
        AnswerOption existing = new AnswerOption();
        existing.setText("Incorrect Option");
        existing.setIsCorrect(false);

        Question oldQuestion = new Question();
        oldQuestion.setId(1L);
        existing.setQuestion(oldQuestion);

        AnswerOptionRequest request = new AnswerOptionRequest();
        request.setText("Correct Answer");
        request.setIsCorrect(true);
        request.setQuestionId(2L);

        Question newQuestion = new Question();
        newQuestion.setId(2L);

        when(answerOptionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(newQuestion));
        when(answerOptionRepository.save(existing)).thenReturn(existing);

        // When
        AnswerOption updated = answerOptionService.updateExistingAnswerOption(1L, request);

        // Then
        assertThat(updated.getText()).isEqualTo("Correct Answer");
        assertThat(updated.getIsCorrect()).isTrue();
        assertThat(updated.getQuestion().getId()).isEqualTo(2L);
        verify(answerOptionRepository).save(existing);
    }

    /**
     * Должен выбросить исключение, если обновляемый вариант ответа не найден.
     */
    @Test
    public void shouldThrowExceptionWhenAnswerOptionNotFoundDuringUpdate() {
        // Given
        AnswerOptionRequest request = new AnswerOptionRequest();
        when(answerOptionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerOptionService.updateExistingAnswerOption(1L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
        verify(answerOptionRepository).findById(1L);
        verify(questionRepository, never()).findById(anyLong());
        verify(answerOptionRepository, never()).save(any());
    }

    /**
     * Должен выбросить исключение при попытке удаления несуществующего варианта ответа.
     */
    @Test
    public void shouldThrowExceptionWhenDeletingNonExistentAnswerOption() {
        // Given
        when(answerOptionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerOptionService.removeAnswerOption(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
        verify(answerOptionRepository).findById(1L);
        verify(answerOptionRepository, never()).delete(any());
    }
}
