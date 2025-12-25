package org.example.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    private String text;
    private String type;
    private Long quizId;
    private List<AnswerOptionRequest> options;
}
