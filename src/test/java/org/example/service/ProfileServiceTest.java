package org.example.service;

import org.example.entity.Profile;
import org.example.entity.User;
import org.example.repository.ProfileRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileService profileService;

    /**
     * Должен создать профиль, если пользователь существует и профиль ещё не создан.
     */
    @Test
    public void shouldCreateProfileWhenUserExistsAndNotAlreadyHasProfile() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setBio("Backend developer");
        profile.setAvatarUrl("https://example.com/avatar.jpg");
        profile.setContactInfo("john.doe@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
            Profile saved = invocation.getArgument(0);
            saved.setId(100L); // имитация автогенерации ID
            return saved;
        });

        // When
        Profile created = profileService.createProfile(profile);

        // Then
        assertThat(created)
                .as("Созданный профиль не должен быть null")
                .isNotNull();
        assertThat(created.getId())
                .as("ID должен быть присвоен")
                .isEqualTo(100L);
        assertThat(created.getUser().getId())
                .as("Пользователь должен быть привязан")
                .isEqualTo(1L);
        assertThat(created.getBio())
                .as("Биография должна совпадать")
                .isEqualTo("Backend developer");
        verify(userRepository).findById(1L);
        verify(profileRepository).save(profile);
    }
}
