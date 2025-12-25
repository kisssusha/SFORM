package org.example.dto.request;

import lombok.Data;

@Data
public class QuizSubmissionRequest {
    private Integer score;
    private Long quizId;
    private Long studentId;
}
