package com.quinter.madness_table.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
