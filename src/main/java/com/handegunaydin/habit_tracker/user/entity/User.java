package com.handegunaydin.habit_tracker.user.entity;

import com.handegunaydin.habit_tracker.common.entity.Item;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Table(name = "users")
@Entity
@Getter
@Setter
public class User extends Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String encodedPassword;

    @Column(unique = true, nullable = false)
    private String mail;

    @Column
    private String mobileNumber;

    @Column
    private LocalDate birthDate;

    @Column
    private boolean enabled =true;

}
