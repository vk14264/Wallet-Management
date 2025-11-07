package com.example.wallet.integration;

import com.example.wallet.model.User;
import com.example.wallet.model.Wallet;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.WalletService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

@SpringBootTest
@Testcontainers
public class WalletServiceIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.6");

    @DynamicPropertySource
    static void setProps(DynamicPropertyRegistry r) {
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    WalletService walletService;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void debitAndCreditAtomic() throws Exception {
        User u = new User("testuser","pass","t@example.com"); userRepository.save(u);
        Wallet w = walletService.createIfNotExists(u.getUsername());
        Wallet saved = walletRepository.findByUsername(u.getUsername()).orElseThrow();
        // credit 100
        walletService.credit(saved.getId(), new BigDecimal("100.00"), "initial");
        Wallet afterCredit = walletRepository.findById(saved.getId()).orElseThrow();
        Assertions.assertEquals(new BigDecimal("100.00"), afterCredit.getBalance());
        // debit 30
        walletService.debit(saved.getId(), new BigDecimal("30.00"), "buy"); 
        Wallet afterDebit = walletRepository.findById(saved.getId()).orElseThrow();
        Assertions.assertEquals(new BigDecimal("70.00"), afterDebit.getBalance());
    }
}
