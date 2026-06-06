package com.payment.auth.controller;

import com.payment.auth.exception.OtpExpiredException;
import com.payment.auth.exception.OtpInvalidException;
import com.payment.auth.exception.OtpLockoutException;
import com.payment.auth.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController — Forgot Password OTP endpoints.
 *
 * POST /api/v2/auth/forgot-password  — request OTP
 * POST /api/v2/auth/verify-otp       — verify OTP
 *
 * Security: both endpoints return identical response time for
 * registered and unregistered emails (timing oracle prevention — TC-OTP-010).
 *
 * Jira: SCRUM-7 · SCRUM-13
 * PR:   #312 — feature/forgot-password-otp → main
 */
@RestController
@RequestMapping("/api/v2/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private OtpService otpService;

    /**
     * POST /api/v2/auth/forgot-password
     *
     * Request body: { "email": "user@example.com" }
     * Response: 200 OK always (prevents user enumeration)
     *
     * TC-OTP-010: Same response for registered and unregistered emails.
     * TC-OTP-014: Email delivery failure returns 503 (not 200).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email address is required."));
        }

        try {
            otpService.requestOtp(email.toLowerCase().trim());
            return ResponseEntity.ok(Map.of("message", "If this email is registered, you will receive an OTP shortly."));
        } catch (Exception e) {
            // EmailDeliveryException or other failures — return 503
            log.error("OTP request failed for email prefix={}", email.charAt(0), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Unable to send OTP. Please try again later."));
        }
    }

    /**
     * POST /api/v2/auth/verify-otp
     *
     * Request body: { "email": "user@example.com", "otp": "123456" }
     * Response 200: { "token": "eyJ..." }
     * Response 400: invalid or expired OTP
     * Response 429: account locked
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp   = body.get("otp");

        if (email == null || otp == null || otp.length() != 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Valid email and 6-digit OTP are required."));
        }

        try {
            String token = otpService.verifyOtp(email.toLowerCase().trim(), otp);
            return ResponseEntity.ok(Map.of("token", token));

        } catch (OtpLockoutException e) {
            log.warn("Locked account OTP attempt. email prefix={}", email.charAt(0));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", e.getMessage()));

        } catch (OtpExpiredException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (OtpInvalidException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
