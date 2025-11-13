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
        User u = new User("testuser", "pass", "t@example.com");
        userRepository.save(u);
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

    @Test
    void transferAtomicBetweenWallets() {
        // Create sender and receiver users
        User sender = new User("alice", "pass", "a@example.com");
        User receiver = new User("bob", "pass", "b@example.com");
        userRepository.save(sender);
        userRepository.save(receiver);

        Wallet fromWallet = walletService.createIfNotExists(sender.getUsername());
        Wallet toWallet = walletService.createIfNotExists(receiver.getUsername());

        // Credit sender with 200
        walletService.credit(fromWallet.getId(), new BigDecimal("200.00"), "Initial load");

        // Perform transfer of 50
        walletService.transfer(fromWallet.getId(), toWallet.getId(), new BigDecimal("50.00"), "Payment");

        // Verify balances after transfer
        Wallet afterFrom = walletRepository.findById(fromWallet.getId()).orElseThrow();
        Wallet afterTo = walletRepository.findById(toWallet.getId()).orElseThrow();

        Assertions.assertEquals(new BigDecimal("150.00"), afterFrom.getBalance());
        Assertions.assertEquals(new BigDecimal("50.00"), afterTo.getBalance());
    }

    @Test
    void transferShouldRollbackOnInsufficientFunds() {
        User u1 = new User("ravi", "pass", "r@example.com");
        User u2 = new User("neha", "pass", "n@example.com");
        userRepository.save(u1);
        userRepository.save(u2);

        Wallet from = walletService.createIfNotExists(u1.getUsername());
        Wallet to = walletService.createIfNotExists(u2.getUsername());

        walletService.credit(from.getId(), new BigDecimal("30.00"), "initial");

        Assertions.assertThrows(RuntimeException.class, () ->
                walletService.transfer(from.getId(), to.getId(), new BigDecimal("50.00"), "fail case"));

        Wallet afterFrom = walletRepository.findById(from.getId()).orElseThrow();
        Wallet afterTo = walletRepository.findById(to.getId()).orElseThrow();

        // Balances should remain unchanged
        Assertions.assertEquals(new BigDecimal("30.00"), afterFrom.getBalance());
        Assertions.assertEquals(BigDecimal.ZERO, afterTo.getBalance());
    }
}

