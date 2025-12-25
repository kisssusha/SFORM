package org.example.service;

import org.example.dto.request.LessonRequest;
import org.example.entity.Lesson;
import org.example.entity.Module;
import org.example.repository.LessonRepository;
import org.example.repository.ModuleRepository;
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
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private LessonService lessonService;

    /**
     * Должен вернуть список всех уроков при вызове getAllLessons.
     */
    @Test
    public void shouldReturnAllLessonsWhenGetAllIsCalled() {
        // Given
        List<Lesson> lessons = Arrays.asList(
                new Lesson(),
                new Lesson()
        );
        when(lessonRepository.findAll()).thenReturn(lessons);

        // When
        List<Lesson> result = lessonService.getAllLessons();

        // Then
        assertThat(result)
                .as("Список уроков должен содержать 2 элемента")
                .hasSize(2);
        verify(lessonRepository).findAll();
    }

    /**
     * Должен создать урок, если модуль существует.
     */
    @Test
    public void shouldCreateLessonWhenModuleExists() {
        // Given
        Module module = new Module();
        module.setId(1L);
        module.setTitle("Object-Oriented Programming");

        Lesson lesson = new Lesson();
        lesson.setTitle("Lesson 1: Classes and Objects");
        lesson.setContent("How to define classes and create instances.");
        lesson.setModule(module);

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(module));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        Lesson created = lessonService.createLesson(lesson);

        // Then
        assertThat(created)
                .as("Созданный урок не должен быть null")
                .isNotNull();
        assertThat(created.getId())
                .as("ID урока должен быть присвоен")
                .isEqualTo(100L);
        assertThat(created.getTitle())
                .as("Заголовок должен совпадать")
                .isEqualTo("Lesson 1: Classes and Objects");
        assertThat(created.getModule().getId())
                .as("Модуль должен быть привязан")
                .isEqualTo(1L);
        verify(moduleRepository).findById(1L);
        verify(lessonRepository).save(lesson);
    }

    /**
     * Должен обновить урок, если все данные корректны.
     */
    @Test
    public void shouldUpdateLessonWhenValidRequestProvided() {
        // Given
        Lesson existing = new Lesson();
        existing.setTitle("Old: Basics of Java");
        existing.setContent("Outdated content.");

        Module oldModule = new Module();
        oldModule.setId(1L);
        existing.setModule(oldModule);

        LessonRequest request = new LessonRequest();
        request.setTitle("Updated: Inheritance and Polymorphism");
        request.setContent("Learn how to extend classes and override methods.");
        request.setModuleId(2L);

        Module newModule = new Module();
        newModule.setId(2L);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(moduleRepository.findById(2L)).thenReturn(Optional.of(newModule));
        when(lessonRepository.save(existing)).thenReturn(existing);

        // When
        Lesson updated = lessonService.updateLesson(1L, request);

        // Then
        assertThat(updated.getTitle())
                .as("Заголовок должен быть обновлён")
                .isEqualTo("Updated: Inheritance and Polymorphism");
        assertThat(updated.getContent())
                .as("Содержание должно быть обновлено")
                .isEqualTo("Learn how to extend classes and override methods.");
        assertThat(updated.getModule().getId())
                .as("Модуль должен быть изменён")
                .isEqualTo(2L);
        verify(lessonRepository).save(existing);
    }
}
