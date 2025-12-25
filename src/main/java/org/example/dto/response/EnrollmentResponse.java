package org.example.dto.response;

import lombok.Data;
import org.example.dto.nested.CourseInfo;
import org.example.dto.nested.UserInfo;

import java.time.LocalDateTime;

@Data
public class EnrollmentResponse {
    private Long id;
    private UserInfo user;
    private CourseInfo course;
    private LocalDateTime enrollDate;
    private String status;
}
