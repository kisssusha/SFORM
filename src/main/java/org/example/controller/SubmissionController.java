package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.SubmissionContentRequest;
import org.example.dto.request.SubmissionRequest;
import org.example.dto.response.SubmissionResponse;
import org.example.entity.Submission;
import org.example.exception.InvalidRequestException;
import org.example.mapper.SubmissionMapper;
import org.example.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;
    private final SubmissionMapper submissionMapper;

    @PostMapping
    public SubmissionResponse createSubmission(
            @RequestBody SubmissionRequest submissionRequest
    ) {
        if (submissionRequest == null) {
            throw new InvalidRequestException("Assignment is required");
        }
        Submission entity = submissionMapper.toEntity(submissionRequest);
        Submission submission = submissionService.createSubmission(entity);

        return submissionMapper.toResponse(submission);
    }

    @PostMapping("/submit")
    public SubmissionResponse submitAssignment(
            @RequestParam Long assignmentId,
            @RequestParam Long studentId,
            @RequestBody SubmissionContentRequest contentRequest
    ) {
        if (contentRequest == null) {
            throw new InvalidRequestException("Content is required");
        }
        if (assignmentId == null) {
            throw new InvalidRequestException("Assignment ID is required");
        }
        if (studentId == null) {
            throw new InvalidRequestException("Student ID is required");
        }
        Submission submission = submissionService.submitAssignment(
                assignmentId, studentId, contentRequest.getContent()
        );
        return submissionMapper.toResponse(submission);
    }

    @GetMapping("/assignment/{assignmentId}")
    public List<SubmissionResponse> getSubmissionsByAssignmentId(@PathVariable Long assignmentId) {
        if (assignmentId == null) {
            throw new InvalidRequestException("Assignment ID is required");
        }
        List<Submission> submissions = submissionService.getSubmissionsByAssignmentId(assignmentId);
        return submissions.stream()
                .map(submissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/student/{studentId}")
    public List<SubmissionResponse> getSubmissionsByStudentId(@PathVariable Long studentId) {
        if (studentId == null) {
            throw new InvalidRequestException("Student ID is required");
        }
        List<Submission> submissions = submissionService.getSubmissionsByStudentId(studentId);

        return submissions
                .stream()
                .map(submissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<SubmissionResponse> getAllSubmissions() {
        return submissionService.getAll()
                .stream()
                .map(submissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public SubmissionResponse getSubmissionById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Submission ID is required");
        }
        Submission submission = submissionService.getSubmissionById(id);

        return submissionMapper.toResponse(submission);
    }


    @PutMapping("/{id}")
    public SubmissionResponse updateSubmission(
            @PathVariable Long id,
            @RequestBody SubmissionRequest submissionRequest
    ) {
        if (submissionRequest == null) {
            throw new InvalidRequestException("Submission is required");
        }
        Submission updated = submissionService.updateSubmission(id, submissionRequest);

        return submissionMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Submission ID is required");
        }
        submissionService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }
}
