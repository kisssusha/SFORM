package org.example.service;

import org.example.dto.request.ModuleRequest;
import org.example.entity.Course;
import org.example.entity.Module;
import org.example.repository.CourseRepository;
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
public class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ModuleService moduleService;

    /**
     * Должен вернуть список всех модулей при вызове getAllModules.
     */
    @Test
    public void shouldReturnAllModulesWhenGetAllIsCalled() {
        // Given
        List<Module> modules = Arrays.asList(
                new Module(),
                new Module()
        );
        when(moduleRepository.findAll()).thenReturn(modules);

        // When
        List<Module> result = moduleService.getAllModules();

        // Then
        assertThat(result)
                .as("Список модулей должен содержать 2 элемента")
                .hasSize(2);
        verify(moduleRepository).findAll();
    }

    /**
     * Должен создать модуль, если курс существует.
     */
    @Test
    public void shouldCreateModuleWhenCourseExists() {
        // Given
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Java Programming");

        Module module = new Module();
        module.setTitle("Module 1: Basics of Java");
        module.setOrderIndex(1);
        module.setCourse(course);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> {
            Module saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        Module created = moduleService.createModule(module);

        // Then
        assertThat(created)
                .as("Созданный модуль не должен быть null")
                .isNotNull();
        assertThat(created.getId())
                .as("ID модуля должен быть присвоен")
                .isEqualTo(100L);
        assertThat(created.getTitle())
                .as("Заголовок должен совпадать")
                .isEqualTo("Module 1: Basics of Java");
        assertThat(created.getCourse().getId())
                .as("Курс должен быть привязан")
                .isEqualTo(1L);
        verify(courseRepository).findById(1L);
        verify(moduleRepository).save(module);
    }

    /**
     * Должен обновить модуль, если все данные корректны.
     */
    @Test
    public void shouldUpdateModuleWhenValidRequestProvided() {
        // Given
        Module existing = new Module();
        existing.setTitle("Old: Getting Started");
        existing.setOrderIndex(1);

        Course oldCourse = new Course();
        oldCourse.setId(1L);
        existing.setCourse(oldCourse);

        ModuleRequest request = new ModuleRequest();
        request.setTitle("Updated: Advanced Concepts");
        request.setOrderIndex(3);
        request.setCourseId(2L);

        Course newCourse = new Course();
        newCourse.setId(2L);

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(newCourse));
        when(moduleRepository.save(existing)).thenReturn(existing);

        // When
        Module updated = moduleService.updateModule(1L, request);

        // Then
        assertThat(updated.getTitle())
                .as("Заголовок должен быть обновлён")
                .isEqualTo("Updated: Advanced Concepts");
        assertThat(updated.getOrderIndex())
                .as("Порядковый номер должен быть изменён")
                .isEqualTo(3);
        assertThat(updated.getCourse().getId())
                .as("Курс должен быть изменён")
                .isEqualTo(2L);
        verify(moduleRepository).save(existing);
    }
}