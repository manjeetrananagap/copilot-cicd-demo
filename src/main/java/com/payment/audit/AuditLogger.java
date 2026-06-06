package com.payment.audit;

import com.payment.exception.AuditWriteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * AuditLogger — writes PCI-DSS compliance audit entries.
 *
 * Every payment transaction MUST produce one audit entry.
 * This class is the sole writer to the payment_audit table.
 *
 * PCI-DSS Req 10.3: Protect audit trails against modification and destruction.
 *
 * Jira: SCRUM-8 | PR: #847
 */
@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Write a payment audit entry.
     *
     * @param transactionId Internal transaction UUID
     * @param userId        Customer identifier
     * @param amount        Amount charged
     * @param gatewayRef    Gateway reference returned on successful charge
     * @param timestamp     Time of transaction completion
     * @throws AuditWriteException if the write fails — caller MUST re-throw this
     */
    public void writePaymentAudit(String transactionId, String userId,
                                   BigDecimal amount, String gatewayRef,
                                   Instant timestamp) throws AuditWriteException {
        try {
            String sql = "INSERT INTO payment_audit " +
                         "(transaction_id, user_id, amount, gateway_ref, event_type, created_at) " +
                         "VALUES (?, ?, ?, ?, 'PAYMENT_PROCESSED', ?)";
            jdbcTemplate.update(sql, transactionId, userId, amount, gatewayRef, timestamp);
            log.info("Audit entry written for transactionId={}", transactionId);
        } catch (Exception e) {
            log.error("AUDIT WRITE FAILURE — transactionId={} userId={}", transactionId, userId, e);
            throw new AuditWriteException(
                "Failed to write payment audit entry for transactionId=" + transactionId,
                transactionId,
                "PAYMENT_PROCESSED",
                e
            );
        }
    }
}
