package com.flolecinc.inkvitebackend.exceptions.verificationcode;

import java.util.UUID;

public class ExpiredVerificationCodeException extends RuntimeException {

    public ExpiredVerificationCodeException(UUID projectId) {
        super("Expired verification code for project with ID: " + projectId);
    }

}
