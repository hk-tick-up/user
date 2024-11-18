package com.example.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {
    public enum Gender {
        MALE, FEMALE
    }
    public enum UserRole{
        ROLE_USER, ROLE_ADMIN
    }
    @Id
    private String id;
    @NonNull
    private String nickname;
    @NonNull
    private String password;
    private int age;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String job;
    private int point;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "delete_request_at")
    private LocalDateTime deleteRequestAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new HashSet<>();
}
