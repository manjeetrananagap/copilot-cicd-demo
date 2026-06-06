package com.payment.auth.service;
import com.payment.auth.exception.EmailDeliveryException;
import org.springframework.stereotype.Service;
/**
 * EmailService — sends transactional emails.
 * TC-OTP-014: Must throw EmailDeliveryException on failure — never swallow.
 */
@Service
public class EmailService {
    public void sendOtpEmail(String email, String otp) throws EmailDeliveryException {
        // Stub — real impl: SendGrid / AWS SES
        // On failure: throw new EmailDeliveryException("Failed to send OTP email to " + email, cause);
    }
}
