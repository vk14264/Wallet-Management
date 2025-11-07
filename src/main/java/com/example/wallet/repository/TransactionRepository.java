package com.example.wallet.repository;

import com.example.wallet.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByWalletIdOrderByTimestampDesc(String walletId);
}
