package com.example.wallet.repository;

import com.example.wallet.model.BlacklistedToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BlacklistedTokenRepository extends MongoRepository<BlacklistedToken, String> {
    Optional<BlacklistedToken> findByToken(String token);
}
