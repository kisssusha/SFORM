package org.example.dto.response;

import lombok.Data;
import org.example.dto.nested.ModuleInfo;

@Data
public class LessonResponse {
    private Long id;
    private String title;
    private String content;
    private ModuleInfo module;
}
