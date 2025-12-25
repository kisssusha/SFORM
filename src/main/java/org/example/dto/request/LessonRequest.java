package org.example.dto.request;

import lombok.Data;

@Data
public class LessonRequest {
    private String title;
    private String content;
    private Long moduleId;
}
