package org.example.mapper;

import org.example.dto.nested.CategoryInfo;
import org.example.dto.nested.UserInfo;
import org.example.dto.request.CourseRequest;
import org.example.dto.response.CourseResponse;
import org.example.entity.Category;
import org.example.entity.Course;
import org.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class, CategoryMapper.class})
public interface CourseMapper {

    @Mapping(target = "teacher", source = "teacherId", qualifiedByName = "teacherIdToUser")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryIdToCategory")
    Course toEntity(CourseRequest request);

    @Mapping(target = "teacher", source = "teacher", qualifiedByName = "userToTeacherInfo")
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryToCategoryInfo")
    CourseResponse toResponse(Course course);

    @Named("userToTeacherInfo")
    default UserInfo userToTeacherInfo(User user) {
        if (user == null) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setName(user.getName());
        return userInfo;
    }

    @Named("teacherIdToUser")
    default User teacherIdToUser(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        User user = new User();
        user.setId(teacherId);
        return user;
    }

    @Named("categoryToCategoryInfo")
    default CategoryInfo categoryToCategoryInfo(Category category) {
        if (category == null) {
            return null;
        }
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.setId(category.getId());
        categoryInfo.setName(category.getName());
        return categoryInfo;
    }

    @Named("categoryIdToCategory")
    default Category categoryIdToCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }
}
