package com.payment.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * NotificationService — sends transactional emails.
 *
 * Jira: SCRUM-8 | PR: #847
 */
@Component
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Send a payment confirmation email to the customer.
     *
     * @param userId   Customer identifier (used to resolve email address)
     * @param amount   Amount charged
     * @param currency ISO 4217 currency code
     */
    public void sendPaymentConfirmation(String userId, BigDecimal amount, String currency) {
        // Stub: logs only. In production, dispatches via SendGrid / SES.
        log.info("Payment confirmation email queued for userId={} amount={} {}",
                userId, amount, currency);
    }
}
