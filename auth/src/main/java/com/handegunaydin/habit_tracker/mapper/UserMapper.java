package com.handegunaydin.habit_tracker.mapper;

import com.handegunaydin.habit_tracker.dto.UserRegisterDTO;
import com.handegunaydin.habit_tracker.dto.UserRegisterResponseDTO;
import com.handegunaydin.habit_tracker.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRegisterDTO dto);
    UserRegisterResponseDTO toResponse(User user);

}
