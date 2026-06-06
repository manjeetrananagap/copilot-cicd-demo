package com.payment.gateway;

import com.payment.exception.PaymentGatewayException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * PaymentGateway — stub interface to the external payment gateway.
 *
 * In production this would call Stripe / Adyen / Worldpay via HTTPS.
 * This stub simulates the gateway for the demo codebase.
 *
 * Jira: SCRUM-8 | PR: #847
 */
@Component
public class PaymentGateway {

    /**
     * Charge a card.
     *
     * @param cardNumber  PAN — must NEVER be logged by callers
     * @param expiryDate  MM/YY
     * @param cvv         3/4 digit security code
     * @param amount      Amount to charge
     * @param currency    ISO 4217 currency code
     * @return Gateway reference string on success
     * @throws PaymentGatewayException on decline or gateway unavailability
     */
    public String charge(String cardNumber, String expiryDate, String cvv,
                         BigDecimal amount, String currency) throws PaymentGatewayException {
        // Stub: always succeeds in demo. In production, calls external API.
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new PaymentGatewayException("Card number is required", "INVALID_CARD");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentGatewayException("Amount must be positive", "INVALID_AMOUNT");
        }
        // Simulate a gateway reference
        return "GW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
