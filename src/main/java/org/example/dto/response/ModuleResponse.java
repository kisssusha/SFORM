package org.example.dto.response;

import lombok.Data;
import org.example.dto.nested.CourseInfo;

@Data
public class ModuleResponse {
    private Long id;
    private String title;
    private Integer orderIndex;
    private CourseInfo course;
}
