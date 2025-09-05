package com.flolecinc.inkvitebackend.security.verificationcode;

import com.flolecinc.inkvitebackend.exceptions.notfound.TattooProjectNotFoundException;
import com.flolecinc.inkvitebackend.exceptions.verificationcode.ExpiredVerificationCodeException;
import com.flolecinc.inkvitebackend.exceptions.verificationcode.WrongVerificationCodeException;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VerificationCodeController.class)
class VerificationCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificationCodeService verificationCodeService;

    @Test
    void sendVerificationEmail_nominal_serviceCalledAndStatusOk() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(verificationCodeService.generateAndSendVerificationCode(id, false)).thenReturn(new VerificationCode());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/send")
                        .queryParam("projectId", id.toString()))
                .andExpect(status().isCreated());
    }

    @Test
    void sendVerificationEmail_projectIdNotFound_exceptionHandled() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(verificationCodeService.generateAndSendVerificationCode(id, false)).thenThrow(new TattooProjectNotFoundException(id));

        // When & Then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/send")
                        .queryParam("projectId", id.toString()))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals("{\"error\":\"Tattoo project with ID '" + id + "' not found\"}", result.getResponse().getContentAsString());
    }

    @Test
    void resendVerificationEmail_nominal_serviceCalledAndStatusOk() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(verificationCodeService.generateAndSendVerificationCode(id, true)).thenReturn(new VerificationCode());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/resend")
                        .queryParam("projectId", id.toString()))
                .andExpect(status().isCreated());
    }

    @Test
    void verifyProject_nominal_serviceCalledAndStatusOk() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(verificationCodeService.verify(id, "code")).thenReturn(new TattooProject());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/verify")
                        .queryParam("projectId", id.toString())
                        .queryParam("verificationCode", "code"))
                .andExpect(status().isOk());
    }

    @Test
    void verifyProject_wrongVerificationCode_exceptionHandled() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(verificationCodeService.verify(id, "code")).thenThrow(new WrongVerificationCodeException(id));

        // When & Then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/verify")
                        .queryParam("projectId", id.toString())
                        .queryParam("verificationCode", "code"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals("{\"error\":\"Wrong verification code for project with ID: " + id + "\"}", result.getResponse().getContentAsString());
    }

    @Test
    void verifyProject_expiredVerificationCode_exceptionHandled() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(verificationCodeService.verify(id, "code")).thenThrow(new ExpiredVerificationCodeException(id));

        // When & Then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/verify")
                        .queryParam("projectId", id.toString())
                        .queryParam("verificationCode", "code"))
                .andExpect(status().isRequestTimeout())
                .andReturn();
        assertEquals("{\"error\":\"Expired verification code for project with ID: " + id + "\"}", result.getResponse().getContentAsString());
    }

}