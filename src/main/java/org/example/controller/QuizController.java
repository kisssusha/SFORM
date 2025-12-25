package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.QuizRequest;
import org.example.dto.response.QuizResponse;
import org.example.dto.response.QuizSubmissionResponse;
import org.example.entity.Quiz;
import org.example.entity.QuizSubmission;
import org.example.exception.InvalidRequestException;
import org.example.mapper.QuizMapper;
import org.example.mapper.QuizSubmissionMapper;
import org.example.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;
    private final QuizMapper quizMapper;
    private final QuizSubmissionMapper quizSubmissionMapper;

    @PostMapping
    public QuizResponse createQuiz(@RequestBody QuizRequest quizRequest) {
        if (quizRequest == null) {
            throw new InvalidRequestException("Quiz request cannot be null.");
        }

        Quiz entity = quizMapper.toEntity(quizRequest);
        Quiz quiz = quizService.createQuiz(entity);

        return quizMapper.toResponse(quiz);
    }

    @PostMapping("/{quizId}/take")
    public QuizSubmissionResponse takeQuiz(
            @PathVariable Long quizId,
            @RequestParam Long studentId,
            @RequestBody Map<Long, Long> answers
    ) {
        if (answers.isEmpty()) {
            throw new InvalidRequestException("Answers cannot be empty.");
        }
        QuizSubmission submission = quizService.takeQuiz(studentId, quizId, answers);
        return quizSubmissionMapper.toResponse(submission);
    }

    @GetMapping
    public List<QuizResponse> getAllQuizzes() {
        return quizService.getAll()
                .stream()
                .map(quizMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public QuizResponse getQuizById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Quiz ID cannot be null.");
        }
        Quiz quiz = quizService.getQuizById(id);
        return quizMapper.toResponse(quiz);
    }

    @PutMapping("/{id}")
    public QuizResponse updateQuiz(
            @PathVariable Long id,
            @RequestBody QuizRequest quizRequest
    ) {
        Quiz updated = quizService.updateQuiz(id, quizRequest);
        return quizMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Quiz ID cannot be null.");
        }
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }
}
