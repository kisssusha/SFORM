package org.example.service;

import org.example.dto.request.TagRequest;
import org.example.entity.Tag;
import org.example.exception.ExistEntityException;
import org.example.repository.TagRepository;
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
public class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    /**
     * Должен вернуть список всех тегов при вызове getAll.
     */
    @Test
    public void shouldReturnAllTagsWhenGetAllIsCalled() {
        // Given
        List<Tag> tags = Arrays.asList(
                new Tag(),
                new Tag()
        );
        when(tagRepository.findAll()).thenReturn(tags);

        // When
        List<Tag> result = tagService.getAll();

        // Then
        assertThat(result)
                .as("Список тегов должен содержать 2 элемента")
                .hasSize(2);
        verify(tagRepository).findAll();
    }

    /**
     * Должен вернуть тег по ID, если он существует.
     */
    @Test
    public void shouldReturnTagByIdWhenExists() {
        // Given
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("java");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        // When
        Tag found = tagService.getTagById(1L);

        // Then
        assertThat(found)
                .as("Найденный тег не должен быть null")
                .isNotNull();
        assertThat(found.getId())
                .as("ID тега должен совпадать")
                .isEqualTo(1L);
        assertThat(found.getName())
                .as("Имя тега должно совпадать")
                .isEqualTo("java");
        verify(tagRepository).findById(1L);
    }

    /**
     * Должен создать тег, если он ещё не существует.
     */
    @Test
    public void shouldCreateTagWhenNotAlreadyExists() {
        // Given
        Tag tag = new Tag();
        tag.setName("spring-boot");

        when(tagRepository.save(tag)).thenAnswer(invocation -> {
            Tag saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        Tag created = tagService.createTag(tag);

        // Then
        assertThat(created)
                .as("Созданный тег не должен быть null")
                .isNotNull();
        assertThat(created.getId())
                .as("ID должен быть присвоен")
                .isEqualTo(100L);
        assertThat(created.getName())
                .as("Имя тега должно совпадать")
                .isEqualTo("spring-boot");
        verify(tagRepository).save(tag);
    }

    /**
     * Должен выбросить исключение, если тег с таким именем уже существует (нарушение уникальности).
     */
    @Test
    public void shouldThrowExceptionWhenTagAlreadyExists() {
        // Given
        Tag tag = new Tag();
        tag.setName("java");

        when(tagRepository.save(tag)).thenThrow(DataIntegrityViolationException.class);

        // When & Then
        assertThatThrownBy(() -> tagService.createTag(tag))
                .as("Должно быть выброшено исключение при попытке создать дублирующий тег")
                .isInstanceOf(ExistEntityException.class)
                .hasMessageContaining("Tag with name 'java' already exists");
        verify(tagRepository).save(tag);
    }

    /**
     * Должен обновить имя тега, если он существует.
     */
    @Test
    public void shouldUpdateTagNameWhenValidRequestProvided() {
        // Given
        Tag existing = new Tag();
        existing.setId(1L);
        existing.setName("old-tag");

        TagRequest request = new TagRequest();
        request.setName("new-feature");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tagRepository.save(existing)).thenReturn(existing);

        // When
        Tag updated = tagService.updateTag(1L, request);

        // Then
        assertThat(updated.getName())
                .as("Имя тега должно быть обновлено")
                .isEqualTo("new-feature");
        verify(tagRepository).findById(1L);
        verify(tagRepository).save(existing);
    }

    /**
     * Должен удалить тег, если он существует.
     */
    @Test
    public void shouldDeleteTagWhenExists() {
        // Given
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("deprecated");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        doNothing().when(tagRepository).delete(tag);

        // When
        tagService.deleteTag(1L);

        // Then
        verify(tagRepository).findById(1L);
        verify(tagRepository).delete(tag);
    }
}
