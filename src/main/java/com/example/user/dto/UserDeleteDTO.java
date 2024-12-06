package com.example.user.dto;

public record UserDeleteDTO(
        String id,
        String password,
        boolean willBeDeleted) {
}
