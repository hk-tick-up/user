package com.example.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
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
    @Column(unique = true)
    @NonNull
    private String nickname;
    @NonNull
    private String password;
    private LocalDate birthday;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String job;
    private int point;

    @CreationTimestamp
    @Column(updatable = false, name="created_at")
    private LocalDateTime createdAt;
    @Column(name = "delete_request_at")
    private LocalDateTime deleteRequestAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    public User(String id){
        this.id = id;
    }
}
