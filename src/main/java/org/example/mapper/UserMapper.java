package org.example.mapper;

import org.example.dto.nested.UserInfo;
import org.example.dto.request.UserRequest;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toEntity(UserRequest request);
    UserResponse toResponse(User user);

    @Named("userToUserInfo")
    default UserInfo userToUserInfo(User user) {
        if (user == null) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setName(user.getName());
        return userInfo;
    }

    @Named("studentIdToUser")
    default User studentIdToUser(Long studentId) {
        if (studentId == null) {
            return null;
        }
        User user = new User();
        user.setId(studentId);
        return user;
    }
}
