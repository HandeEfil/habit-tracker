package com.handegunaydin.habit_tracker.mapper;

import com.handegunaydin.habit_tracker.dto.UserProfileDTO;
import com.handegunaydin.habit_tracker.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfileDTO toResponse(User user);
    User updateEntityFromDto(UserProfileDTO userProfileDTO, @MappingTarget User user);
    List<UserProfileDTO> toResponseList(List<User> user);
}
