package com.payment.model;

import java.time.Instant;

/**
 * Immutable result returned by PaymentProcessor.processPayment().
 * Safe to log — contains no PCI-sensitive data.
 *
 * Jira: SCRUM-8 | PR: #847
 */
public class PaymentResult {

    private final String  status;          // "SUCCESS" | "DECLINED" | "FAILED"
    private final String  gatewayReference; // Opaque ref from payment gateway
    private final String  transactionId;   // Internal UUID — same as input
    private final Instant processedAt;

    public PaymentResult(String status, String gatewayReference,
                         String transactionId, Instant processedAt) {
        this.status           = status;
        this.gatewayReference = gatewayReference;
        this.transactionId    = transactionId;
        this.processedAt      = processedAt;
    }

    public String  getStatus()           { return status; }
    public String  getGatewayReference() { return gatewayReference; }
    public String  getTransactionId()    { return transactionId; }
    public Instant getProcessedAt()      { return processedAt; }

    @Override
    public String toString() {
        return "PaymentResult{status='" + status + "', txn='" + transactionId +
               "', gatewayRef='" + gatewayReference + "', at=" + processedAt + '}';
    }
}
