package com.payment.exception;

/**
 * Thrown when the PCI-DSS compliance audit log cannot be written.
 *
 * CONTRACT: This exception MUST always be re-thrown by any caller.
 * Swallowing this exception silently drops the compliance audit trail,
 * violating PCI-DSS Requirement 10.3 (protect audit trails from destruction).
 *
 * A missing audit log entry is a compliance finding that can result in
 * PCI certification failure and regulatory penalties.
 *
 * Jira: SCRUM-8 | PR: #847
 */
public class AuditWriteException extends Exception {

    private final String transactionId;
    private final String auditType;

    public AuditWriteException(String message, String transactionId, String auditType) {
        super(message);
        this.transactionId = transactionId;
        this.auditType     = auditType;
    }

    public AuditWriteException(String message, String transactionId, String auditType, Throwable cause) {
        super(message, cause);
        this.transactionId = transactionId;
        this.auditType     = auditType;
    }

    public String getTransactionId() { return transactionId; }
    public String getAuditType()     { return auditType; }
}
