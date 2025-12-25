package org.example.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentRequest {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer maxScore;
    private Long lessonId;
}
