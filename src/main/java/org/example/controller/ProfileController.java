package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.ProfileRequest;
import org.example.dto.response.ProfileResponse;
import org.example.entity.Profile;
import org.example.mapper.ProfileMapper;
import org.example.service.ProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;


    @PostMapping
    public ProfileResponse createProfile(@RequestBody ProfileRequest profileRequest) {
        if (profileService.profileExists(profileRequest.getUserId())) {
            throw new RuntimeException("Profile already exists");
        }
        Profile entity = profileMapper.toEntity(profileRequest);
        Profile profile = profileService.createProfile(entity);

        return profileMapper.toResponse(profile);
    }

    @GetMapping("/{id}")
    public ProfileResponse getProfileById(@PathVariable Long id) {
        if (!profileService.profileExists(id)) {
            throw new RuntimeException("Profile not found");
        }
        Profile profile = profileService.getProfileById(id);

        return profileMapper.toResponse(profile);
    }

    @PutMapping("/{id}")
    public ProfileResponse updateProfile(
            @PathVariable Long id,
            @RequestBody ProfileRequest profileRequest
    ) {
        if (!profileService.profileExists(id)) {
            throw new RuntimeException("Profile not found");
        }
        Profile entity = profileMapper.toEntity(profileRequest);
        Profile updated = profileService.updateProfile(id, entity);

        return profileMapper.toResponse(updated);
    }
}
