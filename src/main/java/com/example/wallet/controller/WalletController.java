package com.example.wallet.controller;

import com.example.wallet.model.Wallet;
import com.example.wallet.service.WalletService;
import org.bson.types.Decimal128;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;
    public WalletController(WalletService walletService) { this.walletService = walletService; }

    @GetMapping("/me")
    public ResponseEntity<?> myWallet(@AuthenticationPrincipal User principal) {
        Wallet w = walletService.getByUserId(principal.getUsername());
        return ResponseEntity.ok(w);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Decimal128 balance = walletService.getBalance(username);
        return ResponseEntity.ok(Map.of("username", username, "balance", balance));
    }

}
