package org.example.dto.response;

import lombok.Data;
import org.example.dto.nested.AssignmentInfo;
import org.example.dto.nested.UserInfo;

import java.time.LocalDateTime;

@Data
public class SubmissionResponse {
    private Long id;
    private String content;
    private LocalDateTime submittedAt;
    private Integer score;
    private String feedback;
    private AssignmentInfo assignment;
    private UserInfo student;
}
