package com.flolecinc.inkvitebackend.exceptions.files;

public class UnsupportedImageTypeException extends RuntimeException {

    public UnsupportedImageTypeException(String contentType) {
        super("Unsupported image type: " + contentType);
    }

}
