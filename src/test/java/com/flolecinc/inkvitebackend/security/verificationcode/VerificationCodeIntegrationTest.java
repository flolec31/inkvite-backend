package com.flolecinc.inkvitebackend.security.verificationcode;

import com.flolecinc.inkvitebackend.config.AbstractIntegrationTest;
import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtist;
import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtistRepository;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClient;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClientRepository;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectRepository;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VerificationCodeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TattooArtistRepository tattooArtistRepository;

    @Autowired
    private TattooClientRepository tattooClientRepository;

    @Autowired
    private TattooProjectRepository tattooProjectRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    private TattooProject project;

    @BeforeEach
    void setUp() {
        // Clean up
        tattooArtistRepository.deleteAll();
        tattooClientRepository.deleteAll();
        tattooProjectRepository.deleteAll();
        verificationCodeRepository.deleteAll();

        // Save entities
        TattooArtist artist = tattooArtistRepository.save(new TattooArtist("andrea_g", "Andrea G."));
        TattooClient client = tattooClientRepository.save(new TattooClient("John", "Doe", "john.doe@aol.com"));
        project = tattooProjectRepository.save(new TattooProject(
                LocalDate.now().plusDays(30),
                "Dragon tattoo on back",
                "Back",
                artist,
                client
        ));

        // Project is unverified and has no verification code
        assertEquals(TattooProjectStatus.UNVERIFIED, project.getStatus());
        assertNull(project.getVerificationCode());
    }

    @Test
    void verifyProject_nominal() throws Exception {
        // Send code
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/send")
                .contentType(MediaType.APPLICATION_JSON)
                .param("projectId", String.valueOf(project.getId())));
        // Project has a verification code
        project = tattooProjectRepository.findAll().get(0);
        assertNotNull(project.getVerificationCode());
        // Verify project
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .param("projectId", String.valueOf(project.getId()))
                .param("verificationCode", project.getVerificationCode().getCode()));
        // Project is verified
        project = tattooProjectRepository.findAll().get(0);
        assertEquals(TattooProjectStatus.VERIFIED, project.getStatus());
    }

    @Test
    void verifyProject_invalidCode() throws Exception {
        // Send code
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/send")
                .contentType(MediaType.APPLICATION_JSON)
                .param("projectId", String.valueOf(project.getId())));
        // Project has a verification code
        project = tattooProjectRepository.findAll().get(0);
        assertNotNull(project.getVerificationCode());
        // Verify project with invalid code
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .param("projectId", String.valueOf(project.getId()))
                .param("verificationCode", "wrong code!"));
        // Project is still unverified
        project = tattooProjectRepository.findAll().get(0);
        assertEquals(TattooProjectStatus.UNVERIFIED, project.getStatus());
    }

    @Test
    void verifyProject_expiredCode() throws Exception {
        // Give project an expired verification code
        verificationCodeRepository.save(new VerificationCode("ABCDEF", LocalDateTime.now().minusSeconds(42), project));
        // Verify project
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .param("projectId", String.valueOf(project.getId()))
                .param("verificationCode", "ABCDEF"));
        // Project is still unverified
        project = tattooProjectRepository.findAll().get(0);
        assertEquals(TattooProjectStatus.UNVERIFIED, project.getStatus());
    }

    @Test
    void verifyProject_resendCode() throws Exception {
        // Send code
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/send")
                .contentType(MediaType.APPLICATION_JSON)
                .param("projectId", String.valueOf(project.getId())));
        // Project has a verification code
        project = tattooProjectRepository.findAll().get(0);
        assertNotNull(project.getVerificationCode());
        String oldCode = project.getVerificationCode().getCode();
        // Code is resent
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .param("projectId", String.valueOf(project.getId())));
        // Project has a new verification code
        project = tattooProjectRepository.findAll().get(0);
        assertNotNull(project.getVerificationCode());
        String newCode = project.getVerificationCode().getCode();
        assertNotEquals(oldCode, newCode);
        // Using previous code doesn't valid the project
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .param("projectId", String.valueOf(project.getId()))
                .param("verificationCode", oldCode));
        project = tattooProjectRepository.findAll().get(0);
        assertEquals(TattooProjectStatus.UNVERIFIED, project.getStatus());
        // Using new code valid the project
        mockMvc.perform(MockMvcRequestBuilders.post("/api/security/verification-code/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .param("projectId", String.valueOf(project.getId()))
                .param("verificationCode", newCode));
        project = tattooProjectRepository.findAll().get(0);
        assertEquals(TattooProjectStatus.VERIFIED, project.getStatus());
    }

}
