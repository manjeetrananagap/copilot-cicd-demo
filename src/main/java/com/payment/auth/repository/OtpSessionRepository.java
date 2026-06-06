package com.payment.auth.repository;
import com.payment.auth.model.OtpSession;
import java.util.Optional;
public interface OtpSessionRepository {
    Optional<OtpSession> findByEmail(String email);
    OtpSession save(OtpSession session);
    void deleteByEmail(String email);
    void delete(OtpSession session);
}
