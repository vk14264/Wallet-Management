package com.example.wallet.service;

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

    public Decimal128 getBalance(String username) {
        Wallet wallet = walletRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return wallet.getBalance();
    }


    public Wallet getByUserId(String userId) {
        return walletRepository.findByUsername(userId).orElseGet(() -> createIfNotExists(userId));
    }

    public Transaction credit(String walletId, BigDecimal amount, String remark) {
        Query q = Query.query(Criteria.where("id").is(walletId));
        Update u = new Update().inc("balance", new Decimal128(amount)); // Atomic increment
        Wallet updated = mongoOperations.findAndModify(
                q,
                u,
                FindAndModifyOptions.options().returnNew(true),
                Wallet.class
        );
        if (updated == null) throw new IllegalArgumentException("Wallet not found");

        Transaction t = new Transaction(walletId, amount, "CREDIT", remark);
        return transactionRepository.save(t);
    }

    public Transaction debit(String walletId, BigDecimal amount, String remark) {
        // Atomic decrement only if balance >= amount
        Query q = Query.query(Criteria.where("id").is(walletId)
                .and("balance").gte(new Decimal128(amount)));
        Update u = new Update().inc("balance", new Decimal128(amount.negate())); // Atomic decrement
        Wallet updated = mongoOperations.findAndModify(
                q,
                u,
                FindAndModifyOptions.options().returnNew(true),
                Wallet.class
        );
        if (updated == null) throw new IllegalArgumentException("Insufficient balance or wallet not found");

        Transaction t = new Transaction(walletId, amount, "DEBIT", remark);
        return transactionRepository.save(t);
    }

    @Transactional
    public Transaction transfer(String fromWalletId, String toWalletId, Decimal128 amount, String remark) {
        if (fromWalletId.equals(toWalletId)) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }

        Wallet from = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));
        Wallet to = walletRepository.findById(toWalletId)
                .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

        BigDecimal amt = amount.bigDecimalValue();
        BigDecimal fromBalance = from.getBalance().bigDecimalValue();
        BigDecimal toBalance = to.getBalance().bigDecimalValue();

        if (fromBalance.compareTo(amt) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Convert BigDecimal arithmetic results back to Decimal128
        from.setBalance(new Decimal128(fromBalance.subtract(amt)));
        to.setBalance(new Decimal128(toBalance.add(amt)));

        walletRepository.save(from);
        walletRepository.save(to);

        Transaction debitTxn = new Transaction(fromWalletId, amt, "DEBIT", "Transfer to " + toWalletId + " - " + remark);
        Transaction creditTxn = new Transaction(toWalletId, amt, "CREDIT", "Received from " + fromWalletId + " - " + remark);

        transactionRepository.save(debitTxn);
        transactionRepository.save(creditTxn);

        return debitTxn;
    }


    public List<Transaction> history(String walletId) {
        return transactionRepository.findByWalletIdOrderByTimestampDesc(walletId);
    }
}
