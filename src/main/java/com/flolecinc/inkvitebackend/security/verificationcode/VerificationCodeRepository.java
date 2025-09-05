package com.flolecinc.inkvitebackend.security.verificationcode;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
}
