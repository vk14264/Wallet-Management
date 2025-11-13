package com.example.wallet.service;

import com.example.wallet.dto.TransferResponse;
import com.example.wallet.model.Transaction;
import com.example.wallet.model.User;
import com.example.wallet.model.Wallet;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.WalletRepository;
import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MongoOperations mongoOperations;

    public Wallet save(Wallet w) { return walletRepository.save(w); }


    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository, MongoOperations mongoOperations) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.mongoOperations = mongoOperations;
    }

    public Wallet createIfNotExists(String userId) {
        Optional<Wallet> w = walletRepository.findByUsername(userId);
        if (w.isPresent()) return w.get();
        Wallet wallet = new Wallet(userId);
        return walletRepository.save(wallet);
    }

    public BigDecimal getBalance(String username) {
        Wallet wallet = walletRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return wallet.getBalance();
    }


    public Wallet getByUserId(String userId) {
        return walletRepository.findByUsername(userId).orElseGet(() -> createIfNotExists(userId));
    }

    /**
     * Credit money to wallet (Atomic within one document)
     */
    @Transactional
    public Transaction credit(String walletId, BigDecimal amount, String remark) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction txn = new Transaction(walletId, amount, "CREDIT", remark);
        return transactionRepository.save(txn);
    }

    /**
     * Debit money from wallet (with balance check)
     */
    @Transactional
    public Transaction debit(String walletId, BigDecimal amount, String remark) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction txn = new Transaction(walletId, amount, "DEBIT", remark);
        return transactionRepository.save(txn);
    }

    /**
     * Transfer money between two wallets (ACID across both wallets)
     */
    @Transactional
    public TransferResponse transfer(String fromWalletId, String toWalletId, BigDecimal amount, String remark) {
        if (fromWalletId.equals(toWalletId)) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }

        Wallet from = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));
        Wallet to = walletRepository.findById(toWalletId)
                .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Update balances
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        walletRepository.save(from);
        walletRepository.save(to);

        // Create transaction records
        Transaction debitTxn = new Transaction(
                fromWalletId,
                amount,
                "DEBIT",
                "Transfer to " + toWalletId + " - " + remark
        );
        Transaction creditTxn = new Transaction(
                toWalletId,
                amount,
                "CREDIT",
                "Received from " + fromWalletId + " - " + remark
        );

        transactionRepository.save(debitTxn);
        transactionRepository.save(creditTxn);

        // Return both transactions + new balances
        return new TransferResponse(debitTxn, creditTxn, from.getBalance(), to.getBalance());
    }


    public List<Transaction> history(String walletId) {
        return transactionRepository.findByWalletIdOrderByTimestampDesc(walletId);
    }
}
