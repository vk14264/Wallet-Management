package com.example.wallet.controller;

import com.example.wallet.model.Transaction;
import com.example.wallet.service.WalletService;
import org.bson.types.Decimal128;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tx")
public class TransactionController {

    private final WalletService walletService;

    public TransactionController(WalletService walletService) { this.walletService = walletService; }

    @PostMapping("/credit/{walletId}")
    public ResponseEntity<?> credit(@PathVariable String walletId, @RequestParam String amount, @RequestParam(required=false) String remark) {
        Transaction t = walletService.credit(walletId, new BigDecimal(amount), remark);
        return ResponseEntity.ok(t);
    }

    @PostMapping("/debit/{walletId}")
    public ResponseEntity<?> debit(@PathVariable String walletId, @RequestParam String amount, @RequestParam(required=false) String remark) {
        Transaction t = walletService.debit(walletId, new BigDecimal(amount), remark);
        return ResponseEntity.ok(t);
    }

    @GetMapping("/history/{walletId}")
    public ResponseEntity<List<Transaction>> history(@PathVariable String walletId) {
        return ResponseEntity.ok(walletService.history(walletId));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, String> body) {
        String fromWalletId = body.get("fromWalletId");
        String toWalletId = body.get("toWalletId");
        Decimal128 amount = new Decimal128(new BigDecimal(body.get("amount")));
        String remark = body.getOrDefault("remark", "");

        try {
            Transaction t = walletService.transfer(fromWalletId, toWalletId, amount, remark);
            return ResponseEntity.ok(Map.of("status", "success", "transaction", t));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
