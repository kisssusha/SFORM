package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.QuizSubmissionRequest;
import org.example.dto.response.QuizSubmissionResponse;
import org.example.entity.QuizSubmission;
import org.example.exception.InvalidRequestException;
import org.example.mapper.QuizSubmissionMapper;
import org.example.service.QuizSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz-submissions")
@RequiredArgsConstructor
public class QuizSubmissionController {
    private final QuizSubmissionService quizSubmissionService;
    private final QuizSubmissionMapper quizSubmissionMapper;


    @PostMapping("/submit")
    public QuizSubmissionResponse submitQuiz(
            @RequestParam Long quizId,
            @RequestParam Long studentId,
            @RequestParam Integer score
    ) {
        if (quizId == null || studentId == null) {
            throw new InvalidRequestException("Quiz ID and Student ID are required");
        }
        QuizSubmission submission = quizSubmissionService.submitQuiz(quizId, studentId, score);
        return quizSubmissionMapper.toResponse(submission);
    }

    @PostMapping
    public QuizSubmissionResponse createQuizSubmission(
            @RequestBody QuizSubmissionRequest quizSubmissionRequest
    ) {
        if (quizSubmissionRequest == null) {
            throw new InvalidRequestException("Quiz submission data is required");
        }
        QuizSubmission entity = quizSubmissionMapper.toEntity(quizSubmissionRequest);
        QuizSubmission submission = quizSubmissionService.createQuizSubmission(entity);
        return quizSubmissionMapper.toResponse(submission);
    }

    @GetMapping
    public List<QuizSubmissionResponse> getAllQuizSubmissions() {
        return quizSubmissionService.getAll()
                .stream()
                .map(quizSubmissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public QuizSubmissionResponse getQuizSubmissionById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Quiz submission ID is required");
        }
        QuizSubmission submissionById = quizSubmissionService.getQuizSubmissionById(id);

        return quizSubmissionMapper.toResponse(submissionById);
    }


    @GetMapping("/student/{studentId}")
    public List<QuizSubmissionResponse> getSubmissionsByStudentId(@PathVariable Long studentId) {
        if (studentId == null) {
            throw new InvalidRequestException("Student ID is required");
        }

        List<QuizSubmission> byStudentId = quizSubmissionService.getSubmissionsByStudentId(studentId);
        return byStudentId
                .stream()
                .map(quizSubmissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/module/{moduleId}")
    public List<QuizSubmissionResponse> getSubmissionsByModuleId(
            @PathVariable Long moduleId
    ) {
        if (moduleId == null) {
            throw new InvalidRequestException("Module ID is required");
        }
        List<QuizSubmission> submissions = quizSubmissionService.getSubmissionsByModuleId(moduleId);
        return submissions
                .stream()
                .map(quizSubmissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/course/{courseId}")
    public List<QuizSubmissionResponse> getSubmissionsByCourseId(@PathVariable Long courseId) {
        if (courseId == null) {
            throw new InvalidRequestException("Course ID is required");
        }
        List<QuizSubmission> submissions = quizSubmissionService.getSubmissionsByCourseId(courseId);
        return submissions
                .stream()
                .map(quizSubmissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public QuizSubmissionResponse updateQuizSubmission(
            @PathVariable Long id,
            @RequestBody QuizSubmissionRequest quizSubmissionRequest
    ) {
        if (id == null || quizSubmissionRequest == null) {
            throw new InvalidRequestException("Quiz submission ID and data are required");
        }
        QuizSubmission updated = quizSubmissionService.updateQuizSubmission(id, quizSubmissionRequest);
        return quizSubmissionMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuizSubmission(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Quiz submission ID is required");
        }
        quizSubmissionService.deleteQuizSubmission(id);
        return ResponseEntity.noContent().build();
    }
}
