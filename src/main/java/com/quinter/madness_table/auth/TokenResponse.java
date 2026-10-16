package com.quinter.madness_table.auth;

public record TokenResponse(String accesToken, String tokenType, long expiresInSeconds) {
}
