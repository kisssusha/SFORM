package org.example.dto.response;

import lombok.Data;
import org.example.dto.nested.CourseInfo;
import org.example.dto.nested.UserInfo;

import java.time.LocalDateTime;

@Data
public class CourseReviewResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private CourseInfo course;
    private UserInfo student;
}
