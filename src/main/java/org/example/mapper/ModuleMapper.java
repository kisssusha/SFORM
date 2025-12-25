package org.example.mapper;

import org.example.dto.nested.CourseInfo;
import org.example.dto.request.ModuleRequest;
import org.example.dto.response.ModuleResponse;
import org.example.entity.Course;
import org.example.entity.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CourseMapper.class})
public interface ModuleMapper {

    @Mapping(target = "course", source = "courseId", qualifiedByName = "courseIdToCourse")
    Module toEntity(ModuleRequest request);

    @Mapping(target = "course", source = "course", qualifiedByName = "courseToCourseInfo")
    ModuleResponse toResponse(Module module);

    @Named("courseToCourseInfo")
    default CourseInfo courseToCourseInfo(Course course) {
        if (course == null) {
            return null;
        }
        CourseInfo courseInfo = new CourseInfo();
        courseInfo.setId(course.getId());
        courseInfo.setTitle(course.getTitle());
        return courseInfo;
    }

    @Named("courseIdToCourse")
    default Course courseIdToCourse(Long courseId) {
        if (courseId == null) {
            return null;
        }
        Course course = new Course();
        course.setId(courseId);
        return course;
    }
}
