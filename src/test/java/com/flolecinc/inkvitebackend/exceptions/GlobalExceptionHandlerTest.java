package com.flolecinc.inkvitebackend.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    /**
     * As the size of a file/request is checked beforehand by tomcat,
     * this exception cannot be tested as easily as others, with MockMvc.
     * Thus, this simple unit test ensures code coverage.
     */
    @Test
    void handleMaxSizeException() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(-1);

        ResponseEntity<Map<String, String>> response = handler.handleMaxSizeException(exception);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Maximum upload size exceeded", response.getBody().get("error"));
    }

}
