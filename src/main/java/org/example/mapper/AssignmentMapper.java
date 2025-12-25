package org.example.mapper;

import org.example.dto.nested.LessonInfo;
import org.example.dto.request.AssignmentRequest;
import org.example.dto.response.AssignmentResponse;
import org.example.entity.Assignment;
import org.example.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {LessonMapper.class})
public interface AssignmentMapper {

    @Mapping(target = "lesson", source = "lessonId", qualifiedByName = "lessonIdToLesson")
    Assignment toEntity(AssignmentRequest request);

    @Mapping(target = "lesson", source = "lesson", qualifiedByName = "lessonToLessonInfo")
    AssignmentResponse toResponse(Assignment assignment);

    @Named("lessonToLessonInfo")
    default LessonInfo lessonToLessonInfo(Lesson lesson) {
        if (lesson == null) {
            return null;
        }
        LessonInfo lessonInfo = new LessonInfo();
        lessonInfo.setId(lesson.getId());
        lessonInfo.setTitle(lesson.getTitle());
        return lessonInfo;
    }

    @Named("lessonIdToLesson")
    default Lesson lessonIdToLesson(Long lessonId) {
        if (lessonId == null) {
            return null;
        }
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        return lesson;
    }
}
