package com.example.wallet.unitTest;

import com.example.wallet.model.Transaction;
import com.example.wallet.model.Wallet;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceUnitTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet wallet;

    @BeforeEach
    void setup() {
        wallet = new Wallet("1", "vishal", new BigDecimal("100.00"));
    }

    @Test
    void testCreditIncreasesBalance() {
        // Arrange
        when(walletRepository.findById("1")).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.findById("1")).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        Transaction txn = walletService.credit("1", new BigDecimal("50.00"), "Add funds");

        // Assert
        assertEquals(new BigDecimal("150.00"), wallet.getBalance());
        assertEquals("CREDIT", txn.getType());
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testDebitReducesBalance() {
        // Arrange
        when(walletRepository.findById("1")).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.findById("1")).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Transaction txn = walletService.debit("1", new BigDecimal("30.00"), "Purchase");

        // Assert
        assertEquals(new BigDecimal("70.00"), wallet.getBalance());
        assertEquals("DEBIT", txn.getType());
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testDebitFailsIfInsufficientBalance() {
        // Arrange
        when(walletRepository.findById("1")).thenReturn(Optional.of(wallet));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> walletService.debit("1", new BigDecimal("200.00"), "Overdraft"));

        assertEquals("Insufficient balance", ex.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testTransferMovesMoneyBetweenWallets() {
        // Arrange
        Wallet from = new Wallet("1", "alice", new BigDecimal("200.00"));
        Wallet to = new Wallet("2", "bob", new BigDecimal("50.00"));

        when(walletRepository.findById("1")).thenReturn(Optional.of(from));
        when(walletRepository.findById("2")).thenReturn(Optional.of(to));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        walletService.transfer("1", "2", new BigDecimal("100.00"), "Payment");

        // Assert
        assertEquals(new BigDecimal("100.00"), from.getBalance());
        assertEquals(new BigDecimal("150.00"), to.getBalance());

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void testTransferFailsForSameWallet() {
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                walletService.transfer("1", "1", new BigDecimal("10.00"), "Invalid"));

        assertEquals("Cannot transfer to the same wallet", ex.getMessage());
        verify(walletRepository, never()).save(any());
    }
}

