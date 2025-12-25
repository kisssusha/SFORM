package org.example.service;

import org.example.dto.request.UserRequest;
import org.example.entity.User;
import org.example.exception.ExistEntityException;
import org.example.repository.UserRepository;
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
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    /**
     * Должен вернуть список всех пользователей при вызове getAll.
     */
    @Test
    public void shouldReturnAllUsersWhenGetAllIsCalled() {
        // Given
        List<User> users = Arrays.asList(
                new User(),
                new User()
        );
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAll();

        // Then
        assertThat(result)
                .as("Список пользователей должен содержать 2 элемента")
                .hasSize(2);
        verify(userRepository).findAll();
    }

    /**
     * Должен вернуть пользователя по ID, если он существует.
     */
    @Test
    public void shouldReturnUserByIdWhenExists() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setName("Alice Johnson");
        user.setEmail("alice@example.com");
        user.setRole(User.Role.STUDENT);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        User found = userService.getUserById(1L);

        // Then
        assertThat(found)
                .as("Найденный пользователь не должен быть null")
                .isNotNull();
        assertThat(found.getId())
                .as("ID должен совпадать")
                .isEqualTo(1L);
        assertThat(found.getName())
                .as("Имя должно совпадать")
                .isEqualTo("Alice Johnson");
        assertThat(found.getEmail())
                .as("Email должен совпадать")
                .isEqualTo("alice@example.com");
        assertThat(found.getRole())
                .as("Роль должна быть STUDENT")
                .isEqualTo(User.Role.STUDENT);
        verify(userRepository).findById(1L);
    }

    /**
     * Должен создать пользователя, если email ещё не занят.
     */
    @Test
    public void shouldCreateUserWhenEmailIsUnique() {
        // Given
        User user = new User();
        user.setName("Bob Smith");
        user.setEmail("bob@example.com");
        user.setRole(User.Role.TEACHER);

        when(userRepository.save(user)).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        User created = userService.createUser(user);

        // Then
        assertThat(created)
                .as("Созданный пользователь не должен быть null")
                .isNotNull();
        assertThat(created.getId())
                .as("ID должен быть присвоен")
                .isEqualTo(100L);
        assertThat(created.getName())
                .as("Имя должно совпадать")
                .isEqualTo("Bob Smith");
        assertThat(created.getEmail())
                .as("Email должен совпадать")
                .isEqualTo("bob@example.com");
        assertThat(created.getRole())
                .as("Роль должна быть TEACHER")
                .isEqualTo(User.Role.TEACHER);
        verify(userRepository).save(user);
    }

    /**
     * Должен выбросить исключение, если email уже используется (нарушение уникальности).
     */
    @Test
    public void shouldThrowExceptionWhenUserEmailAlreadyExists() {
        // Given
        User user = new User();
        user.setEmail("alice@example.com");

        when(userRepository.save(user)).thenThrow(DataIntegrityViolationException.class);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(user))
                .as("Должно быть выброшено исключение при попытке создать пользователя с существующим email")
                .isInstanceOf(ExistEntityException.class)
                .hasMessageContaining("User with email 'alice@example.com' already exists");
        verify(userRepository).save(user);
    }

    /**
     * Должен обновить данные пользователя, если он существует.
     */
    @Test
    public void shouldUpdateUserWhenValidRequestProvided() {
        // Given
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setEmail("old@example.com");
        existing.setRole(User.Role.STUDENT);

        UserRequest request = new UserRequest();
        request.setName("Updated Name");
        request.setEmail("updated@example.com");
        request.setRole("TEACHER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        // When
        User updated = userService.updateUser(1L, request);

        // Then
        assertThat(updated.getName())
                .as("Имя должно быть обновлено")
                .isEqualTo("Updated Name");
        assertThat(updated.getEmail())
                .as("Email должен быть обновлён")
                .isEqualTo("updated@example.com");
        assertThat(updated.getRole())
                .as("Роль должна быть изменена на TEACHER")
                .isEqualTo(User.Role.TEACHER);
        verify(userRepository).findById(1L);
        verify(userRepository).save(existing);
    }

    /**
     * Должен удалить пользователя, если он существует.
     */
    @Test
    public void shouldDeleteUserWhenExists() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setName("Inactive User");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }
}
