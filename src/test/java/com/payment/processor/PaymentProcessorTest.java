package com.payment.processor;

import com.payment.audit.AuditLogger;
import com.payment.exception.PaymentGatewayException;
import com.payment.gateway.PaymentGateway;
import com.payment.model.PaymentDetails;
import com.payment.model.PaymentResult;
import com.payment.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentProcessor.processPayment()
 *
 * DEMO NOTE: This test class is intentionally incomplete.
 * Missing cases that Claude's reflection loop will flag:
 *   - No test for null paymentDetails (CRITICAL bug at line 89)
 *   - No test for idempotency / duplicate transactionId (CRITICAL bug at line 61)
 *   - No test verifying AuditWriteException is re-thrown (HIGH bug at line 115)
 *
 * Jira: SCRUM-8 | PR: #847
 */
@ExtendWith(MockitoExtension.class)
class PaymentProcessorTest {

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    @Mock private PaymentGateway      paymentGateway;
    @Mock private JdbcTemplate        jdbcTemplate;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogger         auditLogger;
    @Mock private DataSource          dataSource;
    @Mock private Connection          connection;

    private PaymentDetails validPaymentDetails;
    private String         transactionId;
    private String         userId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID().toString();
        userId        = "USER-12345";
        validPaymentDetails = new PaymentDetails(
                "4111111111114242",  // Test Visa PAN
                "12/27",
                "123",
                new BigDecimal("99.99"),
                "GBP",
                "MERCHANT-001"
        );
    }

    // ─── Happy Path ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-PAY-001: Successful payment returns SUCCESS result")
    void processPayment_happyPath_returnsSuccess() throws Exception {
        when(paymentGateway.charge(anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString()))
                .thenReturn("GW-ABCD1234");
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        PaymentResult result = paymentProcessor.processPayment(
                transactionId, validPaymentDetails, userId);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("GW-ABCD1234", result.getGatewayReference());
        assertEquals(transactionId, result.getTransactionId());
        assertNotNull(result.getProcessedAt());

        verify(paymentGateway, times(1)).charge(anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString());
        verify(notificationService, times(1)).sendPaymentConfirmation(eq(userId),
                any(BigDecimal.class), anyString());
        verify(auditLogger, times(1)).writePaymentAudit(eq(transactionId), eq(userId),
                any(BigDecimal.class), eq("GW-ABCD1234"), any());
    }

    // ─── Retry Logic ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-PAY-002: Gateway failure retries up to 3 times then throws")
    void processPayment_gatewayFailsAllRetries_throwsException() throws Exception {
        when(paymentGateway.charge(anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString()))
                .thenThrow(new PaymentGatewayException("Gateway unavailable", "GATEWAY_ERROR"));

        assertThrows(PaymentGatewayException.class, () ->
                paymentProcessor.processPayment(transactionId, validPaymentDetails, userId));

        verify(paymentGateway, times(3)).charge(anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("TC-PAY-003: Gateway succeeds on second retry after first failure")
    void processPayment_gatewaySucceedsOnSecondAttempt_returnsSuccess() throws Exception {
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(paymentGateway.charge(anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString()))
                .thenThrow(new PaymentGatewayException("Transient error", "TRANSIENT"))
                .thenReturn("GW-RETRY-OK");

        PaymentResult result = paymentProcessor.processPayment(
                transactionId, validPaymentDetails, userId);

        assertEquals("SUCCESS", result.getStatus());
        verify(paymentGateway, times(2)).charge(anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString());
    }

    // ─── MISSING TESTS (intentional gaps for demo) ───────────────────────────
    //
    // TC-PAY-004: processPayment with null paymentDetails should throw
    //             IllegalArgumentException BEFORE transaction opens.
    //             Currently missing — reflects CRITICAL bug at line 89.
    //
    // TC-PAY-005: Duplicate transactionId should return existing result,
    //             not charge the customer twice. Currently missing —
    //             reflects CRITICAL bug at line 61 (no idempotency check).
    //
    // TC-PAY-006: AuditWriteException should propagate to caller.
    //             Currently missing — reflects HIGH bug at line 115
    //             where the exception is swallowed.
    //
    // These will be flagged by Claude's reflection loop during the demo.
}
