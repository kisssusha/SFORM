package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.QuizRequest;
import org.example.entity.*;
import org.example.entity.Module;
import org.example.exception.EntityNotFoundException;
import org.example.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    public List<Quiz> getAll() {
        List<Quiz> quizzes = quizRepository.findAll();
        log.debug("Fetched {} quiz(es)", quizzes.size());
        return quizzes;
    }

    public Quiz getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Quiz not found: ID=%d", id)
                ));
        log.debug("Fetched Quiz: ID={}, Title='{}', ModuleID={}",
                id, quiz.getTitle(), quiz.getModule().getId());
        return quiz;
    }

    public Quiz createQuiz(Quiz quiz) {
        Long moduleId = quiz.getModule().getId();

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Module not found: ID=%d", moduleId)
                ));

        quiz.setModule(module);
        Quiz saved = quizRepository.save(quiz);

        log.info("Created Quiz: ID={}, Title='{}', ModuleID={}",
                saved.getId(), saved.getTitle(), moduleId);
        return saved;
    }

    public Quiz updateQuiz(Long id, QuizRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Quiz not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getTitle() != null && !request.getTitle().equals(quiz.getTitle())) {
            quiz.setTitle(request.getTitle());
            log.debug("Updated title for Quiz ID={}: '{}'", id, request.getTitle());
            updated = true;
        }

        if (request.getTimeLimit() != null && !request.getTimeLimit().equals(quiz.getTimeLimit())) {
            quiz.setTimeLimit(request.getTimeLimit());
            log.debug("Updated timeLimit for Quiz ID={}: {}", id, request.getTimeLimit());
            updated = true;
        }

        if (request.getModuleId() != null && !request.getModuleId().equals(quiz.getModule().getId())) {
            Module module = moduleRepository.findById(request.getModuleId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Module not found: ID=%d", request.getModuleId())
                    ));
            quiz.setModule(module);
            log.debug("Updated module for Quiz ID={}: ModuleID={}", id, request.getModuleId());
            updated = true;
        }

        if (!updated) {
            log.debug("No changes detected for Quiz: ID={}", id);
            return quiz;
        }

        Quiz saved = quizRepository.save(quiz);
        log.info("Successfully updated Quiz: ID={}, Title='{}'", id, saved.getTitle());
        return saved;
    }

    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Quiz not found: ID=%d", id)
                ));

        quizRepository.delete(quiz);

        log.info("Deleted Quiz: ID={}, Title='{}', ModuleID={}",
                id, quiz.getTitle(), quiz.getModule().getId());
    }

    @Transactional
    public QuizSubmission takeQuiz(Long studentId, Long quizId, Map<Long, Long> answers) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Student not found: ID=%d", studentId)
                ));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Quiz not found: ID=%d", quizId)
                ));

        List<Question> questions = questionRepository.findAllByQuiz_Id(quizId);
        if (questions.isEmpty()) {
            log.warn("Quiz ID={} has no questions", quizId);
            throw new IllegalStateException("Quiz has no questions.");
        }

        log.info("Student ID={} started quiz ID={} with {} answers provided", studentId, quizId, answers.size());

        int totalScore = 0;
        for (Question question : questions) {
            Long selectedOptionId = answers.get(question.getId());

            if (selectedOptionId == null) {
                log.debug("No answer provided for Question ID={} in Quiz ID={}", question.getId(), quizId);
                continue;
            }

            AnswerOption selectedOption = answerOptionRepository.findById(selectedOptionId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("AnswerOption not found: ID=%d", selectedOptionId)
                    ));

            if (selectedOption.getIsCorrect()) {
                totalScore += 1;
                log.debug("Correct answer selected: Question ID={}, Option ID={}", question.getId(), selectedOptionId);
            } else {
                log.debug("Incorrect answer selected: Question ID={}, Option ID={}", question.getId(), selectedOptionId);
            }
        }

        QuizSubmission submission = new QuizSubmission();
        submission.setScore(totalScore);
        submission.setQuiz(quiz);
        submission.setStudent(student);

        QuizSubmission saved = quizSubmissionRepository.save(submission);

        log.info("Quiz submitted successfully: SubmissionID={}, StudentID={}, QuizID={}, Score={}/{}",
                saved.getId(), studentId, quizId, totalScore, questions.size());

        return saved;
    }
}
