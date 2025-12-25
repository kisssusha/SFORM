package org.example.dto.response;

import lombok.Data;
import org.example.dto.nested.CategoryInfo;
import org.example.dto.nested.UserInfo;

import java.time.LocalDate;

@Data
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private UserInfo teacher;
    private CategoryInfo category;
    private LocalDate startDate;
    private Integer duration;
}
