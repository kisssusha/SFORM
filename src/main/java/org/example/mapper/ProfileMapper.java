package org.example.mapper;

import org.example.dto.nested.UserInfo;
import org.example.dto.request.ProfileRequest;
import org.example.dto.response.ProfileResponse;
import org.example.entity.Profile;
import org.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface ProfileMapper {

    @Mapping(target = "user", source = "userId", qualifiedByName = "userIdToUser")
    Profile toEntity(ProfileRequest request);

    @Mapping(target = "user", source = "user", qualifiedByName = "profileUserToUserInfo")
    ProfileResponse toResponse(Profile profile);

    @Named("profileUserToUserInfo")
    default UserInfo profileUserToUserInfo(User user) {
        if (user == null) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setName(user.getName());
        return userInfo;
    }

    @Named("userIdToUser")
    default User userIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }
}
