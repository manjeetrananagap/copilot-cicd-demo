package com.payment.processor;

import com.payment.audit.AuditLogger;
import com.payment.exception.AuditWriteException;
import com.payment.exception.PaymentGatewayException;
import com.payment.gateway.PaymentGateway;
import com.payment.model.PaymentDetails;
import com.payment.model.PaymentResult;
import com.payment.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.UUID;

/**
 * PaymentProcessor — core payment execution service.
 *
 * Handles the full atomic payment lifecycle:
 *   1. Charge customer card via payment gateway
 *   2. Update payment ledger in PostgreSQL
 *   3. Send confirmation email
 *   4. Write PCI-DSS audit log entry
 *
 * Jira: SCRUM-8
 * PR:   #847  feature/payment-processor-v2 → main
 *
 * NOTE FOR DEMO: This file contains intentional bugs for the
 * AI in Run Session 1 Dev scenario. Claude will find them via CRISPE review.
 */
@Service
public class PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);

    // MEDIUM — Line 34: SRP violation. Email + audit live in this class.
    // Should be extracted: NotificationService handles email, AuditLogger handles audit.
    @Autowired private PaymentGateway    paymentGateway;
    @Autowired private JdbcTemplate      jdbcTemplate;
    @Autowired private NotificationService notificationService;
    @Autowired private AuditLogger       auditLogger;

    /**
     * Process a payment transaction atomically.
     *
     * @param transactionId  Unique UUID for this transaction. Used for tracing across all systems.
     * @param paymentDetails Contains card details, amount, currency, merchantId.
     * @param userId         Customer identifier. Written to audit trail.
     * @return PaymentResult with status, reference, and timestamp.
     * @throws PaymentGatewayException if the card charge is declined or gateway is unavailable.
     * @throws AuditWriteException     if the compliance audit log cannot be written (always re-throw).
     */
    @Transactional
    public PaymentResult processPayment(String transactionId, PaymentDetails paymentDetails, String userId) {

        // LOW — Line 78 (approx): Magic number. Extract to named constant.
        int maxRetries = 3;

        // CRITICAL BUG #3 — Line 89: No null check on paymentDetails.
        // If null, NullPointerException thrown inside the open @Transactional block.
        // The transaction is left uncommitted → 30s timeout cascade → pool exhaustion.
        // This is the same root-cause pattern as INC-2041 (SCRUM-5).
        log.info("Starting payment processing for transactionId={} userId={}", transactionId, userId);

        // CRITICAL BUG #2 — Line 61: No idempotency check.
        // If the same transactionId is submitted twice (retry storm, network duplicate),
        // this method charges the customer again. Double-payment risk.
        // Fix: check paymentRepository.existsByTransactionId(transactionId) before proceeding.

        PaymentResult result = null;
        int attempt = 0;

        while (attempt < maxRetries) {
            attempt++;
            try {
                // CRITICAL BUG #1 — Line 47: Card number logged in plain text.
                // PCI-DSS Requirement 3.4: Primary Account Number (PAN) must never appear in logs.
                // This is a compliance violation that blocks PCI audit certification.
                log.debug("Processing card: " + paymentDetails.getCardNumber());

                // Step 1: Charge the card
                String gatewayRef = paymentGateway.charge(
                        paymentDetails.getCardNumber(),
                        paymentDetails.getExpiryDate(),
                        paymentDetails.getCvv(),
                        paymentDetails.getAmount(),
                        paymentDetails.getCurrency()
                );

                // Step 2: Update payment ledger
                updateLedger(transactionId, gatewayRef, paymentDetails, userId);

                // Step 3: Send confirmation email (SRP violation — should delegate to NotificationService only)
                notificationService.sendPaymentConfirmation(userId, paymentDetails.getAmount(), paymentDetails.getCurrency());

                // Step 4: Write audit log — MUST be last step before commit (PCI-DSS Req 10.3)
                auditLogger.writePaymentAudit(transactionId, userId, paymentDetails.getAmount(), gatewayRef, Instant.now());

                result = new PaymentResult("SUCCESS", gatewayRef, transactionId, Instant.now());
                log.info("Payment processed successfully transactionId={} gatewayRef={}", transactionId, gatewayRef);
                break;

            } catch (PaymentGatewayException e) {
                log.warn("Gateway attempt {} failed for transactionId={}: {}", attempt, transactionId, e.getMessage());
                if (attempt >= maxRetries) {
                    throw e;
                }

            } catch (AuditWriteException e) {
                // HIGH BUG — Line 115: AuditWriteException is caught and logged but NOT re-thrown.
                // This silently drops the compliance audit log write.
                // PCI-DSS requires a complete, unbroken audit trail.
                // The Javadoc above says "always re-throw" — this code violates its own contract.
                log.error("Audit write failed for transactionId={} — swallowing exception", transactionId, e);

            } catch (Exception e) {
                // HIGH BUG — Line 102: The DB connection (obtained inside updateLedger) is not
                // explicitly closed in this catch path. It returns to the pool only on JVM GC,
                // not immediately. Under load, this causes connection pool exhaustion —
                // the exact pattern that caused INC-2041 (SCRUM-5).
                log.error("Unexpected failure on attempt {} for transactionId={}", attempt, transactionId, e);
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Payment processing failed after " + maxRetries + " attempts", e);
                }
            }
        }

        return result;
    }

    /**
     * Update the payment ledger. Opens its own connection for write isolation.
     * Connection MUST be closed after use — see HIGH bug at line 102.
     */
    private void updateLedger(String transactionId, String gatewayRef,
                               PaymentDetails details, String userId) throws Exception {
        Connection conn = null;
        try {
            conn = jdbcTemplate.getDataSource().getConnection();
            String sql = "INSERT INTO payment_ledger (transaction_id, gateway_ref, user_id, amount, currency, created_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, transactionId);
            ps.setString(2, gatewayRef);
            ps.setString(3, userId);
            ps.setBigDecimal(4, details.getAmount());
            ps.setString(5, details.getCurrency());
            ps.setObject(6, Instant.now());
            ps.executeUpdate();
            conn.commit();
            log.info("Ledger updated for transactionId={}", transactionId);
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception rb) { log.error("Rollback failed", rb); }
            }
            throw e;
            // HIGH BUG — conn is not closed in this catch path.
            // Should use try-with-resources: try (Connection conn = ...) { ... }
        }
        // conn.close() missing in the non-exception path too — resource leak.
    }
}
