package com.flolecinc.inkvitebackend.exceptions.verificationcode;

import java.util.UUID;

public class WrongVerificationCodeException extends RuntimeException {

    public WrongVerificationCodeException(UUID projectId) {
        super("Wrong verification code for project with ID: " + projectId);
    }

}
