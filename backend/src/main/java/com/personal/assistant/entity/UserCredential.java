package com.personal.assistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class UserCredential {
    @Id
    private String userId; // e.g. "me" or email

    @Lob
    @Column(length = 4000)
    private String accessToken;

    @Lob
    @Column(length = 4000)
    private String refreshToken;

    private Long expirationTimeMilliseconds;

    public UserCredential() {
    }

    public UserCredential(String userId, String accessToken, String refreshToken, Long expirationTimeMilliseconds) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTimeMilliseconds = expirationTimeMilliseconds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpirationTimeMilliseconds() {
        return expirationTimeMilliseconds;
    }

    public void setExpirationTimeMilliseconds(Long expirationTimeMilliseconds) {
        this.expirationTimeMilliseconds = expirationTimeMilliseconds;
    }
}
