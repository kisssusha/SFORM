package org.example.dto.response;

import lombok.Data;
import org.example.dto.nested.LessonInfo;

import java.time.LocalDate;

@Data
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer maxScore;
    private LessonInfo lesson;
}
