package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.AnswerOptionRequest;
import org.example.dto.response.AnswerOptionResponse;
import org.example.entity.AnswerOption;
import org.example.exception.InvalidRequestException;
import org.example.mapper.AnswerOptionMapper;
import org.example.service.AnswerOptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/answer-options")
@RequiredArgsConstructor
public class AnswerOptionController {
    private final AnswerOptionService answerOptionService;
    private final AnswerOptionMapper answerOptionMapper;


    @PostMapping
    public AnswerOptionResponse createNewAnswerOption(@RequestBody AnswerOptionRequest answerOptionRequest) {
        if (answerOptionRequest == null) {
            throw new InvalidRequestException("AnswerOptionRequest cannot be null");
        }
        AnswerOption answerOption = answerOptionMapper.toEntity(answerOptionRequest);
        AnswerOption createdAnswerOption = answerOptionService.createAnswerOption(answerOption);
        return answerOptionMapper.toResponse(createdAnswerOption);
    }

    @GetMapping("/{id}")
    public AnswerOptionResponse fetchAnswerOptionById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("AnswerOption ID cannot be null");
        }
        AnswerOption answerOption = answerOptionService.fetchAnswerOptionById(id);
        return answerOptionMapper.toResponse(answerOption);
    }

    @GetMapping
    public List<AnswerOptionResponse> fetchAllAnswerOptions() {
        if (answerOptionService.fetchAllAnswerOptions() == null) {
            throw new InvalidRequestException("AnswerOption list cannot be null");
        }
        return answerOptionService.fetchAllAnswerOptions().stream()
                .map(answerOptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public AnswerOptionResponse updateExistingAnswerOption(
            @PathVariable Long id,
            @RequestBody AnswerOptionRequest answerOptionRequest
    ) {
        if (id == null || answerOptionRequest == null) {
            throw new InvalidRequestException("AnswerOption ID or AnswerOptionRequest cannot be null");
        }
        AnswerOption answerOption = answerOptionService.updateExistingAnswerOption(id, answerOptionRequest);
        return answerOptionMapper.toResponse(answerOption);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeAnswerOption(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("AnswerOption ID cannot be null");
        }
        answerOptionService.removeAnswerOption(id);
        return ResponseEntity.noContent().build();
    }
}
