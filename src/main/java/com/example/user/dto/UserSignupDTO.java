package com.example.user.dto;

import com.example.user.entity.User;

import java.time.LocalDate;

public record UserSignupDTO (
        String id,
        String password,
        String nickname,
        LocalDate birthday,
        User.Gender gender,
        String job
) {
}
