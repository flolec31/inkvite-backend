package com.flolecinc.inkvitebackend.exceptions;

import com.flolecinc.inkvitebackend.exceptions.files.FileReaderException;
import com.flolecinc.inkvitebackend.exceptions.files.S3UploadException;
import com.flolecinc.inkvitebackend.exceptions.files.UnsupportedImageTypeException;
import com.flolecinc.inkvitebackend.exceptions.notfound.RessourceNotFoundException;
import com.flolecinc.inkvitebackend.exceptions.verificationcode.ExpiredVerificationCodeException;
import com.flolecinc.inkvitebackend.exceptions.verificationcode.WrongVerificationCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTattooArtistNotFoundException(RessourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorBody(exception));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(createErrorBody(exception));
    }

    @ExceptionHandler(FileReaderException.class)
    public ResponseEntity<Map<String, String>> handleImageUploadException(FileReaderException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorBody(exception));
    }

    @ExceptionHandler(UnsupportedImageTypeException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedImageTypeException(UnsupportedImageTypeException exception) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(createErrorBody(exception));
    }

    @ExceptionHandler(S3UploadException.class)
    public ResponseEntity<Map<String, String>> handleS3UploadException(S3UploadException exception) {
        return ResponseEntity.status(HttpStatus.valueOf(exception.getStatusCode()))
                .body(createErrorBody(exception));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException exception) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(createErrorBody(exception));
    }

    @ExceptionHandler(WrongVerificationCodeException.class)
    public ResponseEntity<Map<String, String>> handleWrongVerificationCodeException(WrongVerificationCodeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorBody(exception));
    }

    @ExceptionHandler(ExpiredVerificationCodeException.class)
    public ResponseEntity<Map<String, String>> handleExpiredVerificationCodeException(ExpiredVerificationCodeException exception) {
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(createErrorBody(exception));
    }

    private Map<String, String> createErrorBody(Exception exception) {
        return Map.of("error", exception.getMessage());
    }

}
