package com.example.wallet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "blacklisted_tokens")
public class BlacklistedToken {
    @Id
    private String id;
    private String token;
    private Instant expiry;

    public BlacklistedToken() {}
    public BlacklistedToken(String token, Instant expiry) { this.token = token; this.expiry = expiry; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Instant getExpiry() { return expiry; }
    public void setExpiry(Instant expiry) { this.expiry = expiry; }
}
