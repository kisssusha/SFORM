package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.QuizSubmissionRequest;
import org.example.entity.Quiz;
import org.example.entity.QuizSubmission;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.repository.QuizRepository;
import org.example.repository.QuizSubmissionRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(QuizSubmissionService.class);

    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    public List<QuizSubmission> getAll() {
        List<QuizSubmission> submissions = quizSubmissionRepository.findAll();
        log.debug("Fetched {} quiz submission(s)", submissions.size());
        return submissions;
    }

    public QuizSubmission getQuizSubmissionById(Long id) {
        QuizSubmission submission = quizSubmissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("QuizSubmission not found: ID=%d", id)
                ));
        log.debug("Fetched QuizSubmission: ID={}, StudentID={}, QuizID={}, Score={}",
                id, submission.getStudent().getId(), submission.getQuiz().getId(), submission.getScore());
        return submission;
    }

    public List<QuizSubmission> getSubmissionsByStudentId(Long studentId) {
        List<QuizSubmission> submissions = quizSubmissionRepository.findByStudentId(studentId);
        log.debug("Fetched {} submission(s) for Student ID={}", submissions.size(), studentId);
        return submissions;
    }

    public List<QuizSubmission> getSubmissionsByCourseId(Long courseId) {
        List<Quiz> quizzes = quizRepository.findByModule_CourseId(courseId);
        if (quizzes.isEmpty()) {
            log.debug("No quizzes found for Course ID={}", courseId);
            return List.of();
        }

        List<Long> quizIds = quizzes.stream().map(Quiz::getId).collect(Collectors.toList());
        List<QuizSubmission> submissions = quizSubmissionRepository.findByQuizIdIn(quizIds);

        log.debug("Fetched {} submission(s) for Course ID={} across {} quiz(es)", submissions.size(), courseId, quizIds.size());
        return submissions;
    }

    public List<QuizSubmission> getSubmissionsByModuleId(Long moduleId) {
        List<Quiz> quizzes = quizRepository.findByModuleId(moduleId);
        if (quizzes.isEmpty()) {
            log.debug("No quizzes found for Module ID={}", moduleId);
            return List.of();
        }

        List<Long> quizIds = quizzes.stream().map(Quiz::getId).collect(Collectors.toList());
        List<QuizSubmission> submissions = quizSubmissionRepository.findByQuizIdIn(quizIds);

        log.debug("Fetched {} submission(s) for Module ID={} across {} quiz(es)", submissions.size(), moduleId, quizIds.size());
        return submissions;
    }

    public QuizSubmission createQuizSubmission(QuizSubmission quizSubmission) {
        QuizSubmission saved = quizSubmissionRepository.save(quizSubmission);
        log.info("Created QuizSubmission: ID={}, StudentID={}, QuizID={}, Score={}",
                saved.getId(), saved.getStudent().getId(), saved.getQuiz().getId(), saved.getScore());
        return saved;
    }

    public QuizSubmission submitQuiz(Long quizId, Long studentId, Integer score) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Quiz not found: ID=%d", quizId)
                ));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found: ID=%d", studentId)
                ));

        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setScore(score);
        submission.setTakenAt(LocalDateTime.now());

        QuizSubmission saved = quizSubmissionRepository.save(submission);

        log.info("Quiz submitted: SubmissionID={}, StudentID={}, QuizID={}, Score={}",
                saved.getId(), studentId, quizId, score);
        return saved;
    }

    public QuizSubmission updateQuizSubmission(Long id, QuizSubmissionRequest request) {
        QuizSubmission submission = quizSubmissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("QuizSubmission not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getScore() != null && !request.getScore().equals(submission.getScore())) {
            submission.setScore(request.getScore());
            log.debug("Updated score for QuizSubmission ID={}: {}", id, request.getScore());
            updated = true;
        }

        if (request.getQuizId() != null && !request.getQuizId().equals(submission.getQuiz().getId())) {
            Quiz quiz = quizRepository.findById(request.getQuizId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Quiz not found: ID=%d", request.getQuizId())
                    ));
            submission.setQuiz(quiz);
            log.debug("Updated quiz for QuizSubmission ID={}: QuizID={}", id, request.getQuizId());
            updated = true;
        }

        if (request.getStudentId() != null && !request.getStudentId().equals(submission.getStudent().getId())) {
            User student = userRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("User not found: ID=%d", request.getStudentId())
                    ));
            submission.setStudent(student);
            log.debug("Updated student for QuizSubmission ID={}: StudentID={}", id, request.getStudentId());
            updated = true;
        }

        if (!updated) {
            log.debug("No changes detected for QuizSubmission: ID={}", id);
            return submission;
        }

        QuizSubmission saved = quizSubmissionRepository.save(submission);
        log.info("Successfully updated QuizSubmission: ID={}, Score={}", id, saved.getScore());
        return saved;
    }

    public void deleteQuizSubmission(Long id) {
        QuizSubmission submission = quizSubmissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("QuizSubmission not found: ID=%d", id)
                ));

        quizSubmissionRepository.delete(submission);

        log.info("Deleted QuizSubmission: ID={}, StudentID={}, QuizID={}, Score={}",
                id, submission.getStudent().getId(), submission.getQuiz().getId(), submission.getScore());
    }
}
