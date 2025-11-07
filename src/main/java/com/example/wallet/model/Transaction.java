package com.example.wallet.model;

import org.bson.types.Decimal128;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String walletId;
    private BigDecimal amount;
    private String type; // CREDIT or DEBIT
    private Instant timestamp;
    private String remark;

    public Transaction() {}

    public Transaction(String walletId, BigDecimal amount, String type, String remark) {
        this.walletId = walletId;
        this.amount = amount;
        this.type = type;
        this.remark = remark;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWalletId() { return walletId; }
    public void setWalletId(String walletId) { this.walletId = walletId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
