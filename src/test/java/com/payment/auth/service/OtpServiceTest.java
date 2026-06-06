package com.payment.auth.service;

import com.payment.auth.exception.OtpExpiredException;
import com.payment.auth.exception.OtpInvalidException;
import com.payment.auth.exception.OtpLockoutException;
import com.payment.auth.model.OtpSession;
import com.payment.auth.repository.OtpSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OtpService.
 *
 * Session 1 QA Demo — test cases mapped to CRISPE output (SCRUM-13 / SCRUM-7):
 *
 * COVERED:
 *   TC-OTP-001: Happy path — valid OTP within 10 min
 *   TC-OTP-003: OTP expired — throws OtpExpiredException
 *   TC-OTP-006: 3 failed attempts triggers lockout
 *   TC-OTP-007: Locked account — throws OtpLockoutException
 *
 * MISSING (intentional gaps — Claude's reflection loop will flag these):
 *   TC-OTP-008: SCRUM-10 — lockout counter not reset after expiry [KNOWN BUG]
 *   TC-OTP-009: OTP reuse after successful verification (single-use enforcement)
 *   TC-OTP-013: Concurrent OTP requests — race condition (both OTPs valid simultaneously)
 *   TC-OTP-014: Email delivery failure — must return 503, not 200
 *
 * Jira: SCRUM-7 · SCRUM-10 · SCRUM-13
 */
@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @InjectMocks private OtpService otpService;
    @Mock private OtpSessionRepository otpRepo;
    @Mock private EmailService         emailService;
    @Mock private BCryptPasswordEncoder encoder;

    private static final String EMAIL   = "user@example.com";
    private static final String RAW_OTP = "123456";

    private OtpSession validSession;

    @BeforeEach
    void setUp() {
        validSession = new OtpSession(EMAIL, "$2a$10$hashedotp", Instant.now().plus(10, ChronoUnit.MINUTES));
        validSession.setAttemptCount(0);
    }

    // ─── Happy Path ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-OTP-001: Valid OTP within 10 min — returns JWT")
    void verifyOtp_validOtpWithinExpiry_returnsJwt() throws Exception {
        when(otpRepo.findByEmail(EMAIL)).thenReturn(Optional.of(validSession));
        when(encoder.matches(RAW_OTP, validSession.getOtpHash())).thenReturn(true);

        String token = otpService.verifyOtp(EMAIL, RAW_OTP);

        assertNotNull(token);
        assertTrue(token.startsWith("eyJ"));
        verify(otpRepo).delete(validSession);  // single-use: session deleted on success
    }

    // ─── Expiry ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-OTP-003: Expired OTP — throws OtpExpiredException")
    void verifyOtp_expiredOtp_throwsOtpExpiredException() {
        OtpSession expired = new OtpSession(EMAIL, "$2a$10$hash", Instant.now().minus(1, ChronoUnit.MINUTES));
        when(otpRepo.findByEmail(EMAIL)).thenReturn(Optional.of(expired));

        assertThrows(OtpExpiredException.class, () -> otpService.verifyOtp(EMAIL, RAW_OTP));
    }

    // ─── Lockout ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-OTP-006: 3 failed attempts triggers lockout")
    void verifyOtp_threeFailedAttempts_triggersLockout() throws Exception {
        validSession.setAttemptCount(2); // already 2 failures
        when(otpRepo.findByEmail(EMAIL)).thenReturn(Optional.of(validSession));
        when(encoder.matches(RAW_OTP, validSession.getOtpHash())).thenReturn(false);

        OtpLockoutException ex = assertThrows(OtpLockoutException.class,
                () -> otpService.verifyOtp(EMAIL, RAW_OTP));

        assertTrue(ex.getMessage().contains("locked"));
        verify(otpRepo).save(validSession);
        assertNotNull(validSession.getLockoutExpiresAt());
    }

    @Test
    @DisplayName("TC-OTP-007: Locked account — throws OtpLockoutException immediately")
    void verifyOtp_lockedAccount_throwsImmediately() {
        validSession.setAttemptCount(3);
        validSession.setLockoutExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        when(otpRepo.findByEmail(EMAIL)).thenReturn(Optional.of(validSession));

        assertThrows(OtpLockoutException.class, () -> otpService.verifyOtp(EMAIL, RAW_OTP));
        verifyNoMoreInteractions(encoder);  // must not even check OTP when locked
    }

    // ─── MISSING TESTS (intentional — Claude reflection loop will add these) ──

    // TC-OTP-008 [KNOWN BUG SCRUM-10]: After lockout expires, 3 new failures should re-lock.
    // Currently missing — reflects the bug in OtpSession.isLocked() + OtpService line 98-105.
    //
    // TC-OTP-009: After successful verifyOtp(), the same OTP submitted again should throw
    // OtpInvalidException ("No active OTP session found"). OTP must be single-use.
    // Currently missing — session.delete() is called on success but this path is not tested.
    //
    // TC-OTP-013: requestOtp() called twice rapidly for same email.
    // Second call should invalidate first OTP. Both OTPs should NOT be valid simultaneously.
    // Currently missing — concurrent access race condition not tested.
    //
    // TC-OTP-014: emailService.sendOtpEmail() throws EmailDeliveryException.
    // Must propagate as 503 — must NOT be swallowed and return 200.
    // Currently missing — email failure path not covered.
}
