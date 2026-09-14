package com.handegunaydin.habit_tracker.auth.mapper;

import com.handegunaydin.habit_tracker.auth.dto.UserRegisterDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserRegisterResponseDTO;
import com.handegunaydin.habit_tracker.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRegisterDTO dto);
    UserRegisterResponseDTO toResponse(User user);

}
