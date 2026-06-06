package com.payment.auth.model;

import java.time.Instant;

/**
 * OTP session — tracks Forgot Password OTP state per user.
 *
 * PCI-DSS note: otpHash must be BCrypt of the plain-text OTP.
 * Plain-text OTP must NEVER be stored.
 *
 * Known bug: SCRUM-10 / INC-1987
 *   lockoutExpiresAt expires but attemptCount is never reset.
 *   Result: after lockout expires the user is never locked out again.
 *
 * Jira: SCRUM-7 (QA demo) · SCRUM-10 (bug) · SCRUM-13 (story)
 */
public class OtpSession {

    private Long    id;
    private String  userEmail;
    private String  otpHash;            // BCrypt hash — 6-digit OTP, never plain text
    private Instant expiresAt;          // 10 min from creation
    private int     attemptCount;       // failed attempts — max 3 before lockout
    private Instant lockoutExpiresAt;   // null = not locked; past = lockout expired

    public OtpSession() {}

    public OtpSession(String userEmail, String otpHash, Instant expiresAt) {
        this.userEmail    = userEmail;
        this.otpHash      = otpHash;
        this.expiresAt    = expiresAt;
        this.attemptCount = 0;
    }

    /** True if the OTP has passed its 10-minute expiry window. */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * True if the account is currently within a lockout period.
     *
     * KNOWN BUG — SCRUM-10 / INC-1987:
     * This method only checks lockoutExpiresAt. It does NOT reset attemptCount
     * when the lockout expires. So if lockoutExpiresAt has passed, isLocked() returns
     * false (correct) but attemptCount is still 3. The next call to verifyOtp() will
     * not re-lock even after 3 new failures because the increment path triggers on
     * (attemptCount >= MAX_ATTEMPTS) which is already true from the previous lockout.
     *
     * The fix is to reset attemptCount = 0 when lockoutExpiresAt < now() in OtpService.
     */
    public boolean isLocked() {
        return lockoutExpiresAt != null && Instant.now().isBefore(lockoutExpiresAt);
    }

    public Long    getId()               { return id; }
    public String  getUserEmail()        { return userEmail; }
    public String  getOtpHash()          { return otpHash; }
    public Instant getExpiresAt()        { return expiresAt; }
    public int     getAttemptCount()     { return attemptCount; }
    public Instant getLockoutExpiresAt() { return lockoutExpiresAt; }

    public void setId(Long id)                                { this.id = id; }
    public void setUserEmail(String userEmail)                { this.userEmail = userEmail; }
    public void setOtpHash(String otpHash)                    { this.otpHash = otpHash; }
    public void setExpiresAt(Instant expiresAt)               { this.expiresAt = expiresAt; }
    public void setAttemptCount(int attemptCount)             { this.attemptCount = attemptCount; }
    public void setLockoutExpiresAt(Instant lockoutExpiresAt) { this.lockoutExpiresAt = lockoutExpiresAt; }
}
