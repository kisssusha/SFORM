package org.example.service;

import org.example.dto.request.CategoryRequest;
import org.example.entity.Category;
import org.example.exception.ExistEntityException;
import org.example.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    /**
     * Должен вернуть список всех категорий при вызове getAll.
     */
    @Test
    public void shouldReturnAllCategoriesWhenGetAllIsCalled() {
        // Given
        List<Category> categories = Arrays.asList(
                new Category(),
                new Category()
        );
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        List<Category> result = categoryService.getAll();

        // Then
        assertThat(result).hasSize(2);
        verify(categoryRepository).findAll();
    }

    /**
     * Должен вернуть категорию по ID, если она существует.
     */
    @Test
    public void shouldReturnCategoryByIdWhenExists() {
        // Given
        Category category = new Category();
        category.setName("Programming");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // When
        Category found = categoryService.getCategoryById(1L);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Programming");
        verify(categoryRepository).findById(1L);
    }

    /**
     * Должен создать новую категорию при корректных данных.
     */
    @Test
    public void shouldCreateCategoryWhenValidDataProvided() {
        // Given
        Category category = new Category();
        category.setName("Web Development");

        when(categoryRepository.save(category)).thenReturn(category);

        // When
        Category created = categoryService.createCategory(category);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Web Development");
        verify(categoryRepository).save(category);
    }

    /**
     * Должен выбросить ExistEntityException при попытке создать категорию с уже существующим именем.
     */
    @Test
    public void shouldThrowExceptionWhenCreatingDuplicateCategory() {
        // Given
        Category category = new Category();
        category.setName("Data Science");

        when(categoryRepository.save(category)).thenThrow(DataIntegrityViolationException.class);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(category))
                .isInstanceOf(ExistEntityException.class)
                .hasMessageContaining("already exists");
        verify(categoryRepository).save(category);
    }

    /**
     * Должен обновить имя категории при корректном запросе.
     */
    @Test
    public void shouldUpdateCategoryNameWhenValidRequestProvided() {
        // Given
        Category existing = new Category();
        existing.setName("Programming");

        CategoryRequest request = new CategoryRequest();
        request.setName("Advanced Programming");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        // When
        Category updated = categoryService.updateCategory(1L, request);

        // Then
        assertThat(updated.getName()).isEqualTo("Advanced Programming");
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(existing);
    }

    /**
     * Должен удалить категорию, если она существует.
     */
    @Test
    public void shouldDeleteCategoryWhenExists() {
        // Given
        Category category = new Category();
        category.setId(1L);
        category.setName("Obsolete Category");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category);
    }
}
