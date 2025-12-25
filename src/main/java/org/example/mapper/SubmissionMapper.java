package org.example.mapper;

import org.example.dto.nested.AssignmentInfo;
import org.example.dto.request.SubmissionRequest;
import org.example.dto.response.SubmissionResponse;
import org.example.entity.Assignment;
import org.example.entity.Submission;
import org.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AssignmentMapper.class, UserMapper.class})
public interface SubmissionMapper {
    @Mapping(target = "assignment", source = "assignmentId", qualifiedByName = "assignmentIdToAssignment")
    @Mapping(target = "student", source = "studentId", qualifiedByName = "submissionStudentIdToUser")
    Submission toEntity(SubmissionRequest request);

    @Mapping(target = "assignment", source = "assignment", qualifiedByName = "assignmentToAssignmentInfo")
    @Mapping(target = "student", source = "student", qualifiedByName = "userToUserInfo")
    SubmissionResponse toResponse(Submission submission);

    @Named("assignmentToAssignmentInfo")
    default AssignmentInfo assignmentToAssignmentInfo(Assignment assignment) {
        if (assignment == null) {
            return null;
        }
        AssignmentInfo assignmentInfo = new AssignmentInfo();
        assignmentInfo.setId(assignment.getId());
        assignmentInfo.setTitle(assignment.getTitle());
        return assignmentInfo;
    }

    @Named("assignmentIdToAssignment")
    default Assignment assignmentIdToAssignment(Long assignmentId) {
        if (assignmentId == null) {
            return null;
        }
        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        return assignment;
    }

    @Named("submissionStudentIdToUser")
    default User submissionStudentIdToUser(Long studentId) {
        if (studentId == null) {
            return null;
        }
        User user = new User();
        user.setId(studentId);
        return user;
    }
}
