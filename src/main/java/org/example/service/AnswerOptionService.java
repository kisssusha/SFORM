package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.AnswerOptionRequest;
import org.example.entity.AnswerOption;
import org.example.entity.Question;
import org.example.exception.EntityNotFoundException;
import org.example.repository.AnswerOptionRepository;
import org.example.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerOptionService {
    private static final Logger log = LoggerFactory.getLogger(AnswerOptionService.class);

    private final AnswerOptionRepository answerOptionRepository;
    private final QuestionRepository questionRepository;

    public AnswerOption fetchAnswerOptionById(Long id) {
        AnswerOption option = answerOptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("AnswerOption not found: ID=%d", id)
                ));
        log.debug("Fetched AnswerOption: ID={}", id);
        return option;
    }

    public AnswerOption createAnswerOption(AnswerOption answerOption) {
        Long questionId = answerOption.getQuestion().getId();

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Question not found: ID=%d", questionId)
                ));

        answerOption.setQuestion(question);
        AnswerOption saved = answerOptionRepository.save(answerOption);

        log.info("Created AnswerOption: ID={}, Text='{}', QuestionID={}",
                saved.getId(), saved.getText(), questionId);
        return saved;
    }

    public AnswerOption updateExistingAnswerOption(Long id, AnswerOptionRequest request) {
        AnswerOption answerOption = answerOptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("AnswerOption not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getText() != null && !request.getText().equals(answerOption.getText())) {
            answerOption.setText(request.getText());
            log.debug("Updated text for AnswerOption ID={}: '{}'", id, request.getText());
            updated = true;
        }

        if (request.getIsCorrect() != null && !request.getIsCorrect().equals(answerOption.getIsCorrect())) {
            answerOption.setIsCorrect(request.getIsCorrect());
            log.debug("Updated 'isCorrect' for AnswerOption ID={}: {}", id, request.getIsCorrect());
            updated = true;
        }

        if (request.getQuestionId() != null && !request.getQuestionId().equals(answerOption.getQuestion().getId())) {
            Question question = questionRepository.findById(request.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Question not found: ID=%d", request.getQuestionId())
                    ));
            answerOption.setQuestion(question);
            log.debug("Updated question association for AnswerOption ID={}: QuestionID={}", id, request.getQuestionId());
            updated = true;
        }

        AnswerOption saved = answerOptionRepository.save(answerOption);

        if (updated) {
            log.info("Successfully updated AnswerOption: ID={}", id);
        } else {
            log.debug("No changes detected for AnswerOption: ID={}", id);
        }

        return saved;
    }

    public List<AnswerOption> fetchAllAnswerOptions() {
        List<AnswerOption> options = answerOptionRepository.findAll();
        log.debug("Fetched {} AnswerOptions", options.size());
        return options;
    }

    public void removeAnswerOption(Long id) {
        AnswerOption answerOption = answerOptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("AnswerOption not found: ID=%d", id)
                ));

        answerOptionRepository.delete(answerOption);
        log.info("Deleted AnswerOption: ID={}, Text='{}', QuestionID={}",
                id, answerOption.getText(), answerOption.getQuestion().getId());
    }
}
