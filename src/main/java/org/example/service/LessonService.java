package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.LessonRequest;
import org.example.entity.Lesson;
import org.example.entity.Module;
import org.example.exception.EntityNotFoundException;
import org.example.repository.LessonRepository;
import org.example.repository.ModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private static final Logger log = LoggerFactory.getLogger(LessonService.class);

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;

    public List<Lesson> getAllLessons() {
        List<Lesson> lessons = lessonRepository.findAll();
        log.debug("Fetched {} lesson(s)", lessons.size());
        return lessons;
    }

    public Lesson getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Lesson not found: ID=%d", id)
                ));
        log.debug("Fetched Lesson: ID={}, Title='{}', ModuleID={}",
                id, lesson.getTitle(), lesson.getModule().getId());
        return lesson;
    }

    public Lesson createLesson(Lesson lesson) {
        Long moduleId = lesson.getModule().getId();

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Module not found: ID=%d", moduleId)
                ));

        lesson.setModule(module);
        Lesson saved = lessonRepository.save(lesson);

        log.info("Created Lesson: ID={}, Title='{}', ModuleID={}",
                saved.getId(), saved.getTitle(), moduleId);
        return saved;
    }

    public Lesson updateLesson(Long id, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Lesson not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getTitle() != null && !request.getTitle().equals(lesson.getTitle())) {
            lesson.setTitle(request.getTitle());
            log.debug("Updated title for Lesson ID={}: '{}'", id, request.getTitle());
            updated = true;
        }

        if (request.getContent() != null && !request.getContent().equals(lesson.getContent())) {
            lesson.setContent(request.getContent());
            log.debug("Updated content for Lesson ID={}", id);
            updated = true;
        }

        if (request.getModuleId() != null && !request.getModuleId().equals(lesson.getModule().getId())) {
            Module module = moduleRepository.findById(request.getModuleId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Module not found: ID=%d", request.getModuleId())
                    ));
            lesson.setModule(module);
            log.debug("Updated module for Lesson ID={}: ModuleID={}", id, request.getModuleId());
            updated = true;
        }

        if (!updated) {
            log.debug("No changes detected for Lesson: ID={}", id);
            return lesson;
        }

        Lesson saved = lessonRepository.save(lesson);
        log.info("Successfully updated Lesson: ID={}, Title='{}'", id, saved.getTitle());
        return saved;
    }

    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Lesson not found: ID=%d", id)
                ));

        lessonRepository.delete(lesson);

        log.info("Deleted Lesson: ID={}, Title='{}', ModuleID={}",
                id, lesson.getTitle(), lesson.getModule().getId());
    }
}
