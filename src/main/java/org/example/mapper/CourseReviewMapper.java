package org.example.mapper;

import org.example.dto.nested.CourseInfo;
import org.example.dto.request.CourseReviewRequest;
import org.example.dto.response.CourseReviewResponse;
import org.example.entity.Course;
import org.example.entity.CourseReview;
import org.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CourseMapper.class, UserMapper.class})
public interface CourseReviewMapper {

    @Mapping(target = "course", source = "courseId", qualifiedByName = "courseIdToCourse")
    @Mapping(target = "student", source = "studentId", qualifiedByName = "courseReviewStudentIdToUser")
    CourseReview toEntity(CourseReviewRequest request);

    @Mapping(target = "course", source = "course", qualifiedByName = "courseToCourseInfo")
    @Mapping(target = "student", source = "student", qualifiedByName = "userToUserInfo")
    CourseReviewResponse toResponse(CourseReview courseReview);

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

    @Named("courseReviewStudentIdToUser")
    default User courseReviewStudentIdToUser(Long studentId) {
        if (studentId == null) {
            return null;
        }
        User user = new User();
        user.setId(studentId);
        return user;
    }
}
