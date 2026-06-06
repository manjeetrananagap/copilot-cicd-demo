package com.payment.auth.service;

import com.payment.auth.exception.OtpExpiredException;
import com.payment.auth.exception.OtpInvalidException;
import com.payment.auth.exception.OtpLockoutException;
import com.payment.auth.model.OtpSession;
import com.payment.auth.repository.OtpSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * OTP Service — Forgot Password OTP lifecycle management.
 *
 * Endpoints:
 *   POST /api/v2/auth/forgot-password  → calls requestOtp()
 *   POST /api/v2/auth/verify-otp       → calls verifyOtp()
 *
 * Security:
 *   - OTP: 6-digit numeric, SecureRandom
 *   - Storage: BCrypt hash only — plain OTP never persists
 *   - Expiry: 10 minutes
 *   - Lockout: 3 failed attempts → 15-minute lockout
 *   - Email delivery failure must NOT be swallowed (TC-OTP-014)
 *   - Response timing must be identical for registered and unregistered emails (TC-OTP-010)
 *
 * KNOWN BUG — SCRUM-10 / INC-1987:
 *   Lockout counter (attemptCount) is NOT reset when lockout period expires.
 *   After lockout expiry, 3 more failed OTP attempts do NOT trigger a new lockout.
 *   See: OtpSession.isLocked() + verifyOtp() lines 98–105.
 *
 * Jira: SCRUM-7 · SCRUM-10 · SCRUM-13
 * PR:   #312 — feature/forgot-password-otp → main
 */
@Service
public class OtpService {

    private static final Logger       log    = LoggerFactory.getLogger(OtpService.class);
    private static final int          OTP_EXPIRY_MIN  = 10;
    private static final int          MAX_ATTEMPTS    = 3;
    private static final int          LOCKOUT_MIN     = 15;
    private static final SecureRandom RANDOM          = new SecureRandom();

    @Autowired private OtpSessionRepository  otpRepo;
    @Autowired private EmailService          emailService;
    @Autowired private BCryptPasswordEncoder encoder;

    /**
     * Generate and send a 6-digit OTP for the given email.
     *
     * Invalidates any existing OTP session before creating a new one.
     * Always returns 200 OK regardless of email registration status
     * to prevent user enumeration (TC-OTP-010).
     *
     * @param email address to send OTP to
     */
    @Transactional
    public void requestOtp(String email) {
        // Invalidate previous session
        otpRepo.deleteByEmail(email);

        // Generate 6-digit OTP using SecureRandom
        String rawOtp  = String.format("%06d", RANDOM.nextInt(1_000_000));
        String otpHash = encoder.encode(rawOtp);

        OtpSession session = new OtpSession(
                email,
                otpHash,
                Instant.now().plus(OTP_EXPIRY_MIN, ChronoUnit.MINUTES)
        );
        otpRepo.save(session);

        log.info("OTP session created. email prefix={} expiresAt={}",
                email.substring(0, 1), session.getExpiresAt());

        // TC-OTP-014: Email failure must propagate — never swallow
        // EmailService throws EmailDeliveryException on failure → callers get 503
        emailService.sendOtpEmail(email, rawOtp);
    }

    /**
     * Verify OTP submitted by user. Single-use — session deleted on success.
     *
     * @param email  user's email address
     * @param rawOtp 6-digit OTP submitted by user
     * @return JWT token string on successful verification
     * @throws OtpLockoutException  account locked (3 failed attempts)
     * @throws OtpExpiredException  OTP has passed 10-minute window
     * @throws OtpInvalidException  OTP does not match stored hash
     */
    @Transactional
    public String verifyOtp(String email, String rawOtp)
            throws OtpLockoutException, OtpExpiredException, OtpInvalidException {

        OtpSession session = otpRepo.findByEmail(email)
                .orElseThrow(() -> new OtpInvalidException("No active OTP session found."));

        // --- Lockout check ---
        // SCRUM-10 BUG HERE (lines 98-105):
        // isLocked() returns false when lockout has expired — correct.
        // But attemptCount was never reset to 0 after lockout expired.
        // So: user locks at T=0, unlocks at T=15, then at T=16 attempts fail 3 times.
        // The increment (attemptCount++ = 4, 5, 6...) never hits the >= 3 trigger
        // because it was already 3 when lockout expired.
        // Fix: add `if (lockout expired) { session.setAttemptCount(0); }` before this block.
        if (session.isLocked()) {
            log.warn("Attempt on locked account. email prefix={}", email.substring(0, 1));
            throw new OtpLockoutException(
                    "Account locked until " + session.getLockoutExpiresAt() + ". Please try again later.");
        }

        // --- Expiry check ---
        if (session.isExpired()) {
            log.info("Expired OTP submitted. email prefix={}", email.substring(0, 1));
            throw new OtpExpiredException("OTP has expired. Please request a new code.");
        }

        // --- OTP verification ---
        if (!encoder.matches(rawOtp, session.getOtpHash())) {
            int newCount = session.getAttemptCount() + 1;
            session.setAttemptCount(newCount);
            log.warn("Invalid OTP. attempt={}/{} email prefix={}", newCount, MAX_ATTEMPTS, email.substring(0, 1));

            if (newCount >= MAX_ATTEMPTS) {
                session.setLockoutExpiresAt(Instant.now().plus(LOCKOUT_MIN, ChronoUnit.MINUTES));
                otpRepo.save(session);
                throw new OtpLockoutException(
                        "Maximum attempts reached. Account locked for 15 minutes.");
            }

            otpRepo.save(session);
            int remaining = MAX_ATTEMPTS - newCount;
            throw new OtpInvalidException("Incorrect OTP. " + remaining + " attempt(s) remaining.");
        }

        // --- Success — invalidate session (single-use) ---
        otpRepo.delete(session);
        log.info("OTP verified. Session deleted. email prefix={}", email.substring(0, 1));

        // Return JWT — real impl delegates to JwtService
        return generateJwt(email);
    }

    private String generateJwt(String email) {
        // Stub — in production: JwtService.generateToken(userDetails)
        return "eyJhbGciOiJIUzI1NiJ9.stub-payload." + Integer.toHexString(email.hashCode());
    }
}
