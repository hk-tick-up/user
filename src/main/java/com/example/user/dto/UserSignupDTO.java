package com.example.user.dto;

import com.example.user.entity.User;

public record UserSignupDTO (
        String id,
        String password,
        String nickname,
        int age,
        User.Gender gender,
        String job
) {
}
