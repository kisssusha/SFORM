package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.AnswerOptionRequest;
import org.example.dto.request.QuestionRequest;
import org.example.dto.response.QuestionResponse;
import org.example.entity.AnswerOption;
import org.example.entity.Question;
import org.example.exception.InvalidRequestException;
import org.example.mapper.QuestionMapper;
import org.example.service.AnswerOptionService;
import org.example.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final AnswerOptionService answerOptionService;



    @PostMapping
    public QuestionResponse createQuestion(
            @RequestBody QuestionRequest questionRequest
    ) {
        if (questionRequest.getOptions() == null || questionRequest.getOptions().isEmpty()) {
            throw new InvalidRequestException("Options cannot be empty");
        }
        Question entity = questionMapper.toEntity(questionRequest);
        Question question = questionService.createQuestion(entity);

        if (questionRequest.getOptions() != null) {
            for (AnswerOptionRequest request : questionRequest.getOptions()) {
                AnswerOption option = new AnswerOption();
                option.setText(request.getText());
                option.setIsCorrect(request.getIsCorrect());
                option.setQuestion(question);
                answerOptionService.createAnswerOption(option);
            }
        }

        return questionMapper.toResponse(question);
    }

    @GetMapping
    public List<QuestionResponse> getAllQuestions() {
        return questionService.getAll()
                .stream()
                .map(questionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public QuestionResponse getQuestionById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Id cannot be null");
        }
        Question question = questionService.getQuestionById(id);

        return questionMapper.toResponse(question);
    }

    @PutMapping("/{id}")
    public QuestionResponse updateQuestion(
            @PathVariable Long id,
            @RequestBody QuestionRequest questionRequest
    ) {
        if (id == null) {
            throw new InvalidRequestException("Id cannot be null");
        }
        Question updated = questionService.updateQuestion(id, questionRequest);
        return questionMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Id cannot be null");
        }
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
