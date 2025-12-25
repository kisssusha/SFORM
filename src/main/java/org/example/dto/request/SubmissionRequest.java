package org.example.dto.request;

import lombok.Data;

@Data
public class SubmissionRequest {
    private String content;
    private Integer score;
    private String feedback;
    private Long assignmentId;
    private Long studentId;
}
