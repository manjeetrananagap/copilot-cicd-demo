package com.payment.model;

import java.math.BigDecimal;

/**
 * Payment details — contains all card and transaction information.
 *
 * IMPORTANT: Fields here are PCI-DSS sensitive.
 * cardNumber, cvv, expiryDate must NEVER be logged, serialised to unencrypted
 * storage, or included in error messages. Use getMaskedReference() for logging.
 *
 * Jira: SCRUM-8 | PR: #847
 */
public class PaymentDetails {

    private String     cardNumber;    // PAN — PCI-DSS sensitive, never log
    private String     expiryDate;    // MM/YY
    private String     cvv;           // PCI-DSS sensitive, never store post-auth
    private BigDecimal amount;
    private String     currency;      // ISO 4217 e.g. GBP, USD, INR
    private String     merchantId;

    public PaymentDetails() {}

    public PaymentDetails(String cardNumber, String expiryDate, String cvv,
                          BigDecimal amount, String currency, String merchantId) {
        this.cardNumber  = cardNumber;
        this.expiryDate  = expiryDate;
        this.cvv         = cvv;
        this.amount      = amount;
        this.currency    = currency;
        this.merchantId  = merchantId;
    }

    /**
     * Returns a masked reference safe for logging: last 4 digits only.
     * Always use this instead of getCardNumber() in log statements.
     * Example: "****-****-****-4242"
     */
    public String getMaskedReference() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }

    public String     getCardNumber()  { return cardNumber; }
    public String     getExpiryDate()  { return expiryDate; }
    public String     getCvv()         { return cvv; }
    public BigDecimal getAmount()      { return amount; }
    public String     getCurrency()    { return currency; }
    public String     getMerchantId()  { return merchantId; }

    public void setCardNumber(String cardNumber)   { this.cardNumber  = cardNumber; }
    public void setExpiryDate(String expiryDate)   { this.expiryDate  = expiryDate; }
    public void setCvv(String cvv)                 { this.cvv         = cvv; }
    public void setAmount(BigDecimal amount)        { this.amount      = amount; }
    public void setCurrency(String currency)        { this.currency    = currency; }
    public void setMerchantId(String merchantId)   { this.merchantId  = merchantId; }
}
