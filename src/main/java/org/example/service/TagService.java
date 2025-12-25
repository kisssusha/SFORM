package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.TagRequest;
import org.example.entity.Tag;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ExistEntityException;
import org.example.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;

    public List<Tag> getAll() {
        List<Tag> tags = tagRepository.findAll();
        log.debug("Fetched {} tag(s)", tags.size());
        return tags;
    }

    public Tag getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Tag not found: ID=%d", id)
                ));
        log.debug("Fetched Tag: ID={}, Name='{}'", id, tag.getName());
        return tag;
    }

    public Tag createTag(Tag tag) {
        try {
            Tag saved = tagRepository.save(tag);
            log.info("Created Tag: ID={}, Name='{}'", saved.getId(), saved.getName());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            log.warn("Attempt to create duplicate Tag with name: '{}'", tag.getName());
            throw new ExistEntityException(
                    String.format("Tag with name '%s' already exists.", tag.getName())
            );
        }
    }

    public Tag updateTag(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Tag not found: ID=%d", id)
                ));

        if (request.getName() != null && !request.getName().equals(tag.getName())) {
            tag.setName(request.getName());
            log.debug("Updated name for Tag ID={}: '{}'", id, request.getName());
        } else {
            log.debug("No changes detected for Tag: ID={}", id);
            return tag;
        }

        Tag saved = tagRepository.save(tag);
        log.info("Successfully updated Tag: ID={}, Name='{}'", id, saved.getName());
        return saved;
    }

    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Tag not found: ID=%d", id)
                ));

        tagRepository.delete(tag);

        log.info("Deleted Tag: ID={}, Name='{}'", id, tag.getName());
    }
}
