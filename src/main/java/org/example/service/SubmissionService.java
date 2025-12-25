package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.SubmissionRequest;
import org.example.entity.Assignment;
import org.example.entity.Submission;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ExistEntityException;
import org.example.repository.AssignmentRepository;
import org.example.repository.SubmissionRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public List<Submission> getAll() {
        List<Submission> submissions = submissionRepository.findAll();
        log.debug("Fetched {} submission(s)", submissions.size());
        return submissions;
    }

    public Submission getSubmissionById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Submission not found: ID=%d", id)
                ));
        log.debug("Fetched Submission: ID={}, StudentID={}, AssignmentID={}, SubmittedAt={}",
                id, submission.getStudent().getId(), submission.getAssignment().getId(), submission.getSubmittedAt());
        return submission;
    }

    public List<Submission> getSubmissionsByAssignmentId(Long assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        log.debug("Fetched {} submission(s) for Assignment ID={}", submissions.size(), assignmentId);
        return submissions;
    }

    public List<Submission> getSubmissionsByStudentId(Long studentId) {
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        log.debug("Fetched {} submission(s) for Student ID={}", submissions.size(), studentId);
        return submissions;
    }

    public Submission createSubmission(Submission submission) {
        Submission saved = submissionRepository.save(submission);
        log.info("Created Submission: ID={}, StudentID={}, AssignmentID={}, SubmittedAt={}",
                saved.getId(), saved.getStudent().getId(), saved.getAssignment().getId(), saved.getSubmittedAt());
        return saved;
    }

    public Submission submitAssignment(Long assignmentId, Long studentId, String content) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Assignment not found: ID=%d", assignmentId)
                ));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found: ID=%d", studentId)
                ));

        if (submissionRepository.existsByStudentIdAndAssignmentId(studentId, assignmentId)) {
            log.warn("Student ID={} attempted to re-submit Assignment ID={}", studentId, assignmentId);
            throw new ExistEntityException("Student has already submitted a solution for this assignment.");
        }

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent(content);
        submission.setSubmittedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);

        log.info("Assignment submitted: SubmissionID={}, StudentID={}, AssignmentID={}",
                saved.getId(), studentId, assignmentId);
        return saved;
    }

    public Submission updateSubmission(Long id, SubmissionRequest request) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Submission not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getContent() != null && !request.getContent().equals(submission.getContent())) {
            submission.setContent(request.getContent());
            log.debug("Updated content for Submission ID={}", id);
            updated = true;
        }

        if (request.getScore() != null && !request.getScore().equals(submission.getScore())) {
            submission.setScore(request.getScore());
            log.debug("Updated score for Submission ID={}: {}", id, request.getScore());
            updated = true;
        }

        if (request.getFeedback() != null && !request.getFeedback().equals(submission.getFeedback())) {
            submission.setFeedback(request.getFeedback());
            log.debug("Updated feedback for Submission ID={}", id);
            updated = true;
        }

        if (request.getAssignmentId() != null && !request.getAssignmentId().equals(submission.getAssignment().getId())) {
            Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Assignment not found: ID=%d", request.getAssignmentId())
                    ));
            submission.setAssignment(assignment);
            log.debug("Updated assignment for Submission ID={}: AssignmentID={}", id, request.getAssignmentId());
            updated = true;
        }

        if (request.getStudentId() != null && !request.getStudentId().equals(submission.getStudent().getId())) {
            User student = userRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("User not found: ID=%d", request.getStudentId())
                    ));
            submission.setStudent(student);
            log.debug("Updated student for Submission ID={}: StudentID={}", id, request.getStudentId());
            updated = true;
        }

        if (!updated) {
            log.debug("No changes detected for Submission: ID={}", id);
            return submission;
        }

        Submission saved = submissionRepository.save(submission);
        log.info("Successfully updated Submission: ID={}, AssignmentID={}, StudentID={}",
                id, saved.getAssignment().getId(), saved.getStudent().getId());
        return saved;
    }

    public void deleteSubmission(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Submission not found: ID=%d", id)
                ));

        submissionRepository.delete(submission);

        log.info("Deleted Submission: ID={}, StudentID={}, AssignmentID={}, Score={}",
                id, submission.getStudent().getId(), submission.getAssignment().getId(), submission.getScore());
    }
}
