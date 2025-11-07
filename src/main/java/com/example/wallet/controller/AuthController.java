package com.example.wallet.controller;

import com.example.wallet.config.JwtUtil;
import com.example.wallet.model.RefreshToken;
import com.example.wallet.model.User;
import com.example.wallet.model.Wallet;
import com.example.wallet.service.TokenService;
import com.example.wallet.service.UserService;
import com.example.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final WalletService walletService ;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    public AuthController(UserService userService, WalletService walletService, JwtUtil jwtUtil, TokenService tokenService) {
        this.userService = userService;
        this.walletService = walletService;
        this.jwtUtil = jwtUtil; this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");
        var u = new User(username, encoder.encode(password), body.getOrDefault("email", username));
        // create wallet with 0 balance
        Wallet wallet = new Wallet(username);
        walletService.save(wallet);
        userService.save(u);
        return ResponseEntity.ok(Map.of("status","registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");
        var opt = userService.findByUsername(username);
        if (opt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error","invalid credentials"));
        var user = opt.get();
        if (!encoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error","invalid credentials"));
        }
        String token = jwtUtil.generateToken(username);
        RefreshToken rt = tokenService.createRefreshToken(username);
        return ResponseEntity.ok(Map.of("accessToken", token, "refreshToken", rt.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String,String> body) {
        String refresh = body.get("refreshToken");
        Optional<RefreshToken> opt = tokenService.findByToken(refresh);
        if (opt.isEmpty() || opt.get().isRevoked() || opt.get().getExpiry().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body(Map.of("error","invalid refresh token"));
        }
        String username = opt.get().getUsername();
        String accessToken = jwtUtil.generateToken(username);
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String,String> body) {
        String accessToken = body.get("accessToken");
        String refreshToken = body.get("refreshToken");
        if (refreshToken != null) tokenService.revokeRefreshToken(refreshToken);
        if (accessToken != null) {
            // parse expiry from token
            try {
                var claims = jwtUtil.validateAndGetClaims(accessToken);
                var exp = claims.getExpiration().toInstant();
                tokenService.blacklistAccessToken(accessToken, exp);
            } catch (Exception e) {
                // ignore parse errors
            }
        }
        return ResponseEntity.ok(Map.of("status","logged_out"));
    }
}
