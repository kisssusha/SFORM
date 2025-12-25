package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.QuestionRequest;
import org.example.entity.Question;
import org.example.entity.Quiz;
import org.example.exception.EntityNotFoundException;
import org.example.repository.QuestionRepository;
import org.example.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public List<Question> getAll() {
        List<Question> questions = questionRepository.findAll();
        log.debug("Fetched {} question(s)", questions.size());
        return questions;
    }

    public Question getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Question not found: ID=%d", id)
                ));
        log.debug("Fetched Question: ID={}, Text='{}', QuizID={}",
                id, question.getText(), question.getQuiz().getId());
        return question;
    }

    public Question createQuestion(Question question) {
        Long quizId = question.getQuiz().getId();

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Quiz not found: ID=%d", quizId)
                ));

        question.setQuiz(quiz);
        Question saved = questionRepository.save(question);

        log.info("Created Question: ID={}, Text='{}', QuizID={}",
                saved.getId(), saved.getText(), quizId);
        return saved;
    }

    public Question updateQuestion(Long id, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Question not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getText() != null && !request.getText().equals(question.getText())) {
            question.setText(request.getText());
            log.debug("Updated text for Question ID={}: '{}'", id, request.getText());
            updated = true;
        }

        if (request.getType() != null && !request.getType().equals(question.getType().name())) {
            try {
                Question.QuestionType type = Question.QuestionType.valueOf(request.getType());
                question.setType(type);
                log.debug("Updated type for Question ID={}: {}", id, type);
                updated = true;
            } catch (IllegalArgumentException e) {
                log.warn("Invalid question type provided: '{}' for Question ID={}", request.getType(), id);
                throw new IllegalArgumentException("Invalid question type: " + request.getType());
            }
        }

        if (request.getQuizId() != null && !request.getQuizId().equals(question.getQuiz().getId())) {
            Quiz quiz = quizRepository.findById(request.getQuizId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Quiz not found: ID=%d", request.getQuizId())
                    ));
            question.setQuiz(quiz);
            log.debug("Updated quiz for Question ID={}: QuizID={}", id, request.getQuizId());
            updated = true;
        }

        if (!updated) {
            log.debug("No changes detected for Question: ID={}", id);
            return question;
        }

        Question saved = questionRepository.save(question);
        log.info("Successfully updated Question: ID={}, Text='{}'", id, saved.getText());
        return saved;
    }

    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Question not found: ID=%d", id)
                ));

        questionRepository.delete(question);

        log.info("Deleted Question: ID={}, Text='{}', QuizID={}",
                id, question.getText(), question.getQuiz().getId());
    }
}
