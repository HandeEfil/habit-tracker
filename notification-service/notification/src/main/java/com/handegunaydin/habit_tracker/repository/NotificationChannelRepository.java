package com.handegunaydin.habit_tracker.repository;

import com.handegunaydin.habit_tracker.entity.NotificationChannelConfiguration;
import com.handegunaydin.habit_tracker.enums.NotificationChannelType;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannelConfiguration, Long> {

    @Query(value = "SELECT distinct ch FROM NotificationChannelConfiguration ncc join ncc.notificationChannels ch where ncc.notificationType = :notificationType and ch IN :allowedChannels ")
    List<NotificationChannelType> findNotificationChannelByEventTypeAndAllowedChannel(@Param("notificationType") NotificationType notificationType, @Param("allowedChannels") List<NotificationChannelType> allowedChannels);
}
