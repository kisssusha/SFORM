package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.AssignmentRequest;
import org.example.entity.Assignment;
import org.example.entity.Lesson;
import org.example.exception.EntityNotFoundException;
import org.example.repository.AssignmentRepository;
import org.example.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;

    public List<Assignment> getAll() {
        List<Assignment> assignments = assignmentRepository.findAll();
        log.debug("Fetched {} assignment(s)", assignments.size());
        return assignments;
    }

    public Assignment getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Assignment not found: ID=%d", id)
                ));
        log.debug("Fetched Assignment: ID={}, Title='{}'", id, assignment.getTitle());
        return assignment;
    }

    public Assignment createAssignment(Assignment assignment) {
        Long lessonId = assignment.getLesson().getId();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Lesson not found: ID=%d", lessonId)
                ));

        assignment.setLesson(lesson);
        Assignment saved = assignmentRepository.save(assignment);

        log.info("Created Assignment: ID={}, Title='{}', LessonID={}",
                saved.getId(), saved.getTitle(), lessonId);
        return saved;
    }

    public Assignment updateAssignment(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Assignment not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getTitle() != null && !request.getTitle().equals(assignment.getTitle())) {
            assignment.setTitle(request.getTitle());
            log.debug("Updated title for Assignment ID={}: '{}'", id, request.getTitle());
            updated = true;
        }

        if (request.getDescription() != null && !request.getDescription().equals(assignment.getDescription())) {
            assignment.setDescription(request.getDescription());
            log.debug("Updated description for Assignment ID={}", id);
            updated = true;
        }

        if (request.getDueDate() != null && !request.getDueDate().equals(assignment.getDueDate())) {
            assignment.setDueDate(request.getDueDate());
            log.debug("Updated dueDate for Assignment ID={}: {}", id, request.getDueDate());
            updated = true;
        }

        if (request.getMaxScore() != null && !request.getMaxScore().equals(assignment.getMaxScore())) {
            assignment.setMaxScore(request.getMaxScore());
            log.debug("Updated maxScore for Assignment ID={}: {}", id, request.getMaxScore());
            updated = true;
        }

        if (request.getLessonId() != null && !request.getLessonId().equals(assignment.getLesson().getId())) {
            Lesson lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Lesson not found: ID=%d", request.getLessonId())
                    ));
            assignment.setLesson(lesson);
            log.debug("Updated lesson association for Assignment ID={}: LessonID={}", id, request.getLessonId());
            updated = true;
        }

        Assignment saved = assignmentRepository.save(assignment);

        if (updated) {
            log.info("Successfully updated Assignment: ID={}", id);
        } else {
            log.debug("No changes detected for Assignment: ID={}", id);
        }

        return saved;
    }

    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Assignment not found: ID=%d", id)
                ));

        assignmentRepository.delete(assignment);
        log.info("Deleted Assignment: ID={}, Title='{}', LessonID={}",
                id, assignment.getTitle(), assignment.getLesson().getId());
    }
}


