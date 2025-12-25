package org.example.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CourseRequest {
    private String title;
    private String description;
    private Long teacherId;
    private Long categoryId;
    private LocalDate startDate;
    private Integer duration;
}
