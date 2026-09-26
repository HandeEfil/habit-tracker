package com.handegunaydin.habit_tracker.repository;

import com.handegunaydin.habit_tracker.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {


    boolean existsByMail(String mail);

    Optional<User> findByMail(String mail);

    @Modifying
    @Transactional
    @Query("Update User r " +
            "set r.enabled = false where r.enabled=true and r.id = :id ")
    Integer updateUserEnabled(@Param(value = "id") Long id);

}
