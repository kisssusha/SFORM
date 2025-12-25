package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.AssignmentRequest;
import org.example.dto.response.AssignmentResponse;
import org.example.entity.Assignment;
import org.example.exception.InvalidRequestException;
import org.example.mapper.AssignmentMapper;
import org.example.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final AssignmentMapper assignmentMapper;

    @PostMapping
    public AssignmentResponse createAssignment(@RequestBody AssignmentRequest assignmentRequest) {
        if (assignmentRequest == null) {
            throw new InvalidRequestException("Assignment request cannot be null");
        }
        Assignment entity = assignmentMapper.toEntity(assignmentRequest);
        Assignment assignment = assignmentService.createAssignment(entity);
        return assignmentMapper.toResponse(assignment);
    }

    @GetMapping
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentService.getAll().
                stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AssignmentResponse getAssignmentById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Assignment id cannot be null");
        }
        Assignment assignment = assignmentService.getAssignmentById(id);
        return assignmentMapper.toResponse(assignment);
    }

    @PutMapping("/{id}")
    public AssignmentResponse updateAssignment(
            @PathVariable Long id,
            @RequestBody AssignmentRequest assignmentRequest
    ) {
        if (id == null || assignmentRequest == null) {
            throw new InvalidRequestException("Assignment id or request cannot be null");
        }
        Assignment assignment = assignmentService.updateAssignment(id, assignmentRequest);
        return assignmentMapper.toResponse(assignment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Assignment id cannot be null");
        }
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

}
