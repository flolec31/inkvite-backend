package com.flolecinc.inkvitebackend.security.verificationcode;

import com.flolecinc.inkvitebackend.emails.MailSender;
import com.flolecinc.inkvitebackend.exceptions.verificationcode.ExpiredVerificationCodeException;
import com.flolecinc.inkvitebackend.exceptions.verificationcode.WrongVerificationCodeException;
import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtist;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClient;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectService;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    @Mock
    private MailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private TattooProjectService tattooProjectService;

    @Spy
    @InjectMocks
    private VerificationCodeService verificationCodeService;

    private VerificationCode verificationCode;

    @BeforeEach
    void setUp() {
        TattooArtist tattooArtist = new TattooArtist();
        tattooArtist.setDisplayName("Andrea G.");
        TattooClient tattooClient = new TattooClient();
        tattooClient.setEmail("john.doe@aol.com");
        TattooProject tattooProject = new TattooProject();
        tattooProject.setTattooArtist(tattooArtist);
        tattooProject.setTattooClient(tattooClient);
        verificationCode = new VerificationCode();
        verificationCode.setTattooProject(tattooProject);
        verificationCode.setCode("ABCDEF");
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(1));
    }

    @Test
    void generateAndSendVerificationCode_nominal_methodsCalled() throws MessagingException, UnsupportedEncodingException {
        // Given
        UUID id = UUID.randomUUID();
        TattooProject project = new TattooProject();
        when(tattooProjectService.findById(id)).thenReturn(project);
        doReturn(null).when(verificationCodeService).generateVerificationCode(project);
        doNothing().when(verificationCodeService).sendVerificationEmail(any());
        when(verificationCodeRepository.save(any())).thenReturn(verificationCode);

        // When
        VerificationCode code = verificationCodeService.generateAndSendVerificationCode(id, false);

        // Then
        verify(verificationCodeRepository, times(0)).delete(any());
        assertNotNull(code);
        assertEquals(verificationCode, code);
    }

    @Test
    void generateAndSendVerificationCode_renewal_methodsCalled() throws MessagingException, UnsupportedEncodingException {
        // Given
        UUID id = UUID.randomUUID();
        TattooProject project = new TattooProject();
        project.setVerificationCode(new VerificationCode());
        when(tattooProjectService.findById(id)).thenReturn(project);
        doReturn(null).when(verificationCodeService).generateVerificationCode(project);
        doNothing().when(verificationCodeService).sendVerificationEmail(any());
        when(verificationCodeRepository.save(any())).thenReturn(verificationCode);

        // When
        VerificationCode code = verificationCodeService.generateAndSendVerificationCode(id, true);

        // Then
        assertNotNull(code);
        assertEquals(verificationCode, code);
    }

    @Test
    void generateAndSendVerificationCode_renewalNoOldCode_methodsCalled() throws MessagingException, UnsupportedEncodingException {
        // Given
        UUID id = UUID.randomUUID();
        TattooProject project = new TattooProject();
        when(tattooProjectService.findById(id)).thenReturn(project);
        doReturn(null).when(verificationCodeService).generateVerificationCode(project);
        doNothing().when(verificationCodeService).sendVerificationEmail(any());
        when(verificationCodeRepository.save(any())).thenReturn(verificationCode);

        // When
        VerificationCode code = verificationCodeService.generateAndSendVerificationCode(id, true);

        // Then
        assertNotNull(code);
        assertEquals(verificationCode, code);
    }

    @Test
    void generateVerificationCode_nominal_verificationCodeGenerated() {
        // Given
        TattooProject tattooProject = new TattooProject();
        doReturn("ABCDEF").when(verificationCodeService).generateRandomCode();

        // When
        VerificationCode code = verificationCodeService.generateVerificationCode(tattooProject);

        // Then
        assertNotNull(code);
        assertEquals("ABCDEF", code.getCode());
        assertTrue(code.getExpiresAt().isBefore(LocalDateTime.now().plus(VerificationCodeService.VERIFICATION_CODE_VALIDITY).plusMinutes(1)));
        assertTrue(code.getExpiresAt().isAfter(LocalDateTime.now().plus(VerificationCodeService.VERIFICATION_CODE_VALIDITY).minusMinutes(1)));
        assertEquals(tattooProject, code.getTattooProject());
    }

    @Test
    void generateRandomCode_nominal_randomCodeGenerated() {
        // When
        String code = verificationCodeService.generateRandomCode();

        // Then
        assertNotNull(code);
        assertEquals(VerificationCodeService.CODE_LENGTH, code.length());
        for (char c : code.toCharArray()) {
            assertTrue(VerificationCodeService.CHARACTERS.contains(String.valueOf(c)));
        }
    }

    @Test
    void sendVerificationEmail_nominal_mailSenderCalled() throws MessagingException, UnsupportedEncodingException {
        // Given
        when(templateEngine.process(anyString(), any())).thenReturn("<p>HTML content</p>");
        doNothing().when(mailSender).sendMail(any(), any(), anyString(), anyString());

        // When
        verificationCodeService.sendVerificationEmail(verificationCode);

        // Then
        ArgumentCaptor<InternetAddress> fromCaptor = ArgumentCaptor.forClass(InternetAddress.class);
        ArgumentCaptor<InternetAddress> toCaptor = ArgumentCaptor.forClass(InternetAddress.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

        verify(mailSender).sendMail(fromCaptor.capture(), toCaptor.capture(), subjectCaptor.capture(), contentCaptor.capture());

        assertEquals("Inkvite", fromCaptor.getValue().getPersonal());
        assertEquals("john.doe@aol.com", toCaptor.getValue().getAddress());
        assertEquals("Confirme ta demande de rendez-vous avec Andrea G.", subjectCaptor.getValue());
        assertEquals("<p>HTML content</p>", contentCaptor.getValue());
    }

    @Test
    void verify_nominal_statusChanged() {
        // Given
        TattooProject tattooProject = new TattooProject();
        tattooProject.setVerificationCode(verificationCode);
        UUID id = UUID.randomUUID();
        when(tattooProjectService.findById(id)).thenReturn(tattooProject);
        when(tattooProjectService.save(tattooProject)).thenReturn(tattooProject);

        // When
        TattooProject savedProject = verificationCodeService.verify(id, "ABCDEF");

        // Then
        assertEquals(TattooProjectStatus.VERIFIED, savedProject.getStatus());
    }

    @Test
    void verify_expiredCode_exceptionThrown() {
        // Given
        TattooProject tattooProject = new TattooProject();
        verificationCode.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tattooProject.setVerificationCode(verificationCode);
        UUID id = UUID.randomUUID();
        tattooProject.setId(id);
        when(tattooProjectService.findById(id)).thenReturn(tattooProject);

        // When & Then
        Exception e = assertThrows(ExpiredVerificationCodeException.class, () -> verificationCodeService.verify(id, "ABCDEF"));
        assertEquals("Expired verification code for project with ID: " + id, e.getMessage());
    }

    @Test
    void verify_wrongCode_exceptionThrown() {
        // Given
        TattooProject tattooProject = new TattooProject();
        tattooProject.setVerificationCode(verificationCode);
        UUID id = UUID.randomUUID();
        tattooProject.setId(id);
        when(tattooProjectService.findById(id)).thenReturn(tattooProject);

        // When & Then
        Exception e = assertThrows(WrongVerificationCodeException.class, () -> verificationCodeService.verify(id, "BADEST"));
        assertEquals("Wrong verification code for project with ID: " + id, e.getMessage());
    }

    @Test
    void verify_statusIsntUnverified_exceptionThrown() {
        // Given
        TattooProject tattooProject = new TattooProject();
        tattooProject.setStatus(TattooProjectStatus.VERIFIED);
        tattooProject.setVerificationCode(verificationCode);
        UUID id = UUID.randomUUID();
        when(tattooProjectService.findById(id)).thenReturn(tattooProject);

        // When / Then
        Exception e = assertThrows(IllegalStateException.class, () -> verificationCodeService.verify(id, "ABCDEF"));
        assertEquals("Cannot verify following status : VERIFIED", e.getMessage());
    }

}