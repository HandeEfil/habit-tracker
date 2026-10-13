package com.handegunaydin.habit_tracker.mapper;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.NotificationDTO;
import com.handegunaydin.habit_tracker.dto.UserFailedLoginAttemptEvent;
import com.handegunaydin.habit_tracker.dto.UserRegisterEvent;
import com.handegunaydin.habit_tracker.entity.FailedLoginEventNotification;
import com.handegunaydin.habit_tracker.entity.Notification;
import com.handegunaydin.habit_tracker.entity.RegisterEventNotification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.SubclassMapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {


    @SubclassMapping(
            source = UserRegisterEvent.class,
            target = RegisterEventNotification.class
    )
    @SubclassMapping(
            source = UserFailedLoginAttemptEvent.class,
            target = FailedLoginEventNotification.class
    )
    Notification fromEvent(DomainEvent event);


    @Mapping(source = "mail", target = "recipient")
    RegisterEventNotification toNotification(UserRegisterEvent event);

    @Mapping(source = "mail", target = "recipient")
    FailedLoginEventNotification toNotification(UserFailedLoginAttemptEvent event);


    @ObjectFactory
    default Notification createNotification(DomainEvent event) {
        throw new IllegalArgumentException("Unsupported event: " + event.getClass());
    }

    NotificationDTO toResponse(Notification notification);

    List<NotificationDTO> toResponseDTOList(List<Notification> notifications);

}
