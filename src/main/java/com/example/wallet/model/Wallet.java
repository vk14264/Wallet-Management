package com.example.wallet.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.Decimal128;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "wallets")
public class Wallet {
    @Getter
    @Id
    private String id;
    private String username;
    @Setter
    private Decimal128 balance = Decimal128.POSITIVE_ZERO;

    public Wallet() {}
    public Wallet(String username) {
        this.username = username;
    }

    public Wallet(String username, BigDecimal bal) {
        this.username = username;
        this.balance = new Decimal128(bal);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return username;
    }
    public void setUserId(String userId) {
        this.username = userId;
    }

    public Decimal128 getBalance() {
        return balance;
    }
}
