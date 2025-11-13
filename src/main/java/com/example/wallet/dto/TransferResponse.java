package com.example.wallet.dto;

import com.example.wallet.model.Transaction;

import java.math.BigDecimal;

public record TransferResponse(
        Transaction debitTransaction,
        Transaction creditTransaction,
        BigDecimal fromWalletBalance,
        BigDecimal toWalletBalance
) {}
