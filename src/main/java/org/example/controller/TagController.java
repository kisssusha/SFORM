package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.TagRequest;
import org.example.dto.response.TagResponse;
import org.example.entity.Tag;
import org.example.exception.InvalidRequestException;
import org.example.mapper.TagMapper;
import org.example.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;
    private final TagMapper tagMapper;

    @PostMapping
    public TagResponse createTag(@RequestBody TagRequest tagRequest) {
        if (tagRequest == null) {
            throw new InvalidRequestException("Tag request cannot be null");
        }
        Tag entity = tagMapper.toEntity(tagRequest);
        Tag tag = tagService.createTag(entity);

        return tagMapper.toResponse(tag);
    }

    @GetMapping
    public List<TagResponse> getAllTags() {
        return tagService
                .getAll()
                .stream()
                .map(tagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TagResponse getTagById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Tag id cannot be null");
        }
        Tag tag = tagService.getTagById(id);

        return tagMapper.toResponse(tag);
    }

    @PutMapping("/{id}")
    public TagResponse updateTag(
            @PathVariable Long id,
            @RequestBody TagRequest tagRequest
    ) {
        if (id == null) {
            throw new InvalidRequestException("Tag id cannot be null");
        }
        Tag updateTag = tagService.updateTag(id, tagRequest);

        return tagMapper.toResponse(updateTag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Tag id cannot be null");
        }
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
