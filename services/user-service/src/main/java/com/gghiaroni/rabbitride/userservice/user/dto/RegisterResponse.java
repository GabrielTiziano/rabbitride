package com.gghiaroni.rabbitride.userservice.user.dto;

import com.gghiaroni.rabbitride.userservice.user.User;

import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String nome,
    String email
) {
    public static RegisterResponse from(User user) {
        return new RegisterResponse(user.getId(), user.getNome(), user.getEmail());
    }
}
