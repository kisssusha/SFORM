package org.example.dto.response;

import lombok.Data;
import org.example.dto.nested.QuizInfo;
import org.example.dto.nested.UserInfo;

import java.time.LocalDateTime;

@Data
public class QuizSubmissionResponse {
    private Long id;
    private Integer score;
    private LocalDateTime takenAt;
    private QuizInfo quiz;
    private UserInfo student;
}
