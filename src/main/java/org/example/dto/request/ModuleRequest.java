package org.example.dto.request;

import lombok.Data;

@Data
public class ModuleRequest {
    private String title;
    private Integer orderIndex;
    private Long courseId;
}
