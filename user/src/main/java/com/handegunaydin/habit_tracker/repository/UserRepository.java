package com.handegunaydin.habit_tracker.repository;

import com.handegunaydin.habit_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {


    boolean existsByMail(String mail);

    Optional<User> findByMail(String mail);

}
