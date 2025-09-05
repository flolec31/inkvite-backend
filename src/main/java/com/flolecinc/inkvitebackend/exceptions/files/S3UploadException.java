package com.flolecinc.inkvitebackend.exceptions.files;

import lombok.Getter;

@Getter
public class S3UploadException extends RuntimeException {

    private final int statusCode;

    public S3UploadException(String message, int statusCode) {
        super("Failed to upload a file to the S3 server: " + message);
        this.statusCode = statusCode;
    }

}
