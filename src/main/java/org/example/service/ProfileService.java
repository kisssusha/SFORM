package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.Profile;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ExistEntityException;
import org.example.repository.ProfileRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public boolean profileExists(Long id) {
        boolean exists = profileRepository.existsById(id);
        if (exists) {
            log.debug("Profile exists: ID={}", id);
        } else {
            log.debug("Profile does not exist: ID={}", id);
        }
        return exists;
    }

    public boolean isUserAlreadyHasProfile(Long userId) {
        boolean exists = profileRepository.existsByUserId(userId);
        if (exists) {
            log.debug("User already has a profile: UserID={}", userId);
        } else {
            log.debug("User does not have a profile: UserID={}", userId);
        }
        return exists;
    }

    public Profile getProfileById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Profile not found: ID=%d", id)
                ));
        log.debug("Fetched Profile: ID={}, UserID={}", id, profile.getUser().getId());
        return profile;
    }

    public Profile createProfile(Profile profile) {
        Long userId = profile.getUser().getId();
        if (userId == null) {
            log.warn("Attempt to create profile with null User ID");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found: ID=%d", userId)
                ));

        if (isUserAlreadyHasProfile(userId)) {
            log.warn("Attempt to create duplicate profile for UserID={}", userId);
            throw new ExistEntityException(
                    String.format("Profile already exists for user ID=%d", userId)
            );
        }

        profile.setUser(user);

        try {
            Profile saved = profileRepository.save(profile);
            log.info("Created Profile: ID={}, UserID={}", saved.getId(), userId);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity violation while creating profile for UserID={}: {}", userId, ex.getMessage());
            throw new ExistEntityException(
                    String.format("Failed to create profile due to data conflict for user ID=%d", userId)
            );
        }
    }

    public Profile updateProfile(Long id, Profile profileDetails) {
        Profile profile = getProfileById(id);
        boolean updated = false;

        if (profileDetails.getBio() != null && !profileDetails.getBio().equals(profile.getBio())) {
            profile.setBio(profileDetails.getBio());
            log.debug("Updated bio for Profile ID={}", id);
            updated = true;
        }

        if (profileDetails.getAvatarUrl() != null && !profileDetails.getAvatarUrl().equals(profile.getAvatarUrl())) {
            profile.setAvatarUrl(profileDetails.getAvatarUrl());
            log.debug("Updated avatar URL for Profile ID={}", id);
            updated = true;
        }

        if (profileDetails.getContactInfo() != null && !profileDetails.getContactInfo().equals(profile.getContactInfo())) {
            profile.setContactInfo(profileDetails.getContactInfo());
            log.debug("Updated contact info for Profile ID={}", id);
            updated = true;
        }

        if (!updated) {
            log.debug("No changes detected for Profile: ID={}", id);
            return profile;
        }

        Profile saved = profileRepository.save(profile);
        log.info("Successfully updated Profile: ID={}, UserID={}", id, profile.getUser().getId());
        return saved;
    }
}
