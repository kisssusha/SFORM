package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.LessonRequest;
import org.example.dto.response.LessonResponse;
import org.example.entity.Lesson;
import org.example.exception.InvalidRequestException;
import org.example.mapper.LessonMapper;
import org.example.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;
    private final LessonMapper lessonMapper;


    @PostMapping
    public LessonResponse createLesson(@RequestBody LessonRequest lessonRequest) {
        if (lessonRequest == null) {
            throw new InvalidRequestException("LessonRequest cannot be null");
        }
        Lesson lesson = lessonMapper.toEntity(lessonRequest);
        Lesson createdLesson = lessonService.createLesson(lesson);
        return lessonMapper.toResponse(createdLesson);
    }

    @GetMapping
    public List<LessonResponse> getAllLessons() {
        return lessonService.getAllLessons()
                .stream()
                .map(lessonMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public LessonResponse getLessonById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("ID cannot be null");
        }
        Lesson lesson = lessonService.getLessonById(id);
        return lessonMapper.toResponse(lesson);
    }

    @PutMapping("/{id}")
    public LessonResponse updateLesson(
            @PathVariable Long id,
            @RequestBody LessonRequest lessonRequest
    ) {
        if (id == null || lessonRequest == null) {
            throw new InvalidRequestException("ID or LessonRequest cannot be null");
        }
        Lesson updatedLesson = lessonService.updateLesson(id, lessonRequest);
        return lessonMapper.toResponse(updatedLesson);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("ID cannot be null");
        }
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}
