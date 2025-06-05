package com.flolecinc.inkvitebackend.security.verificationcode;

import com.flolecinc.inkvitebackend.emails.MailSender;
import com.flolecinc.inkvitebackend.exceptions.verificationcode.ExpiredVerificationCodeException;
import com.flolecinc.inkvitebackend.exceptions.verificationcode.WrongVerificationCodeException;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectService;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    @Value("${inkvite.logo.url}")
    private String logoUrl;

    @Value("${spring.mail.username}")
    private String inkviteEmailAddress;

    public static final int CODE_LENGTH = 6;
    public static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String MAIL_SUBJECT = "Confirme ta demande de rendez-vous avec %s";
    public static final Duration VERIFICATION_CODE_VALIDITY = Duration.ofMinutes(10);

    private final MailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final VerificationCodeRepository verificationCodeRepository;
    private final TattooProjectService tattooProjectService;

    public VerificationCode generateAndSendVerificationCode(UUID projectId, boolean renewal) throws MessagingException, UnsupportedEncodingException {
        TattooProject project = tattooProjectService.findById(projectId);
        if (renewal) {
            VerificationCode oldCode = project.getVerificationCode();
            if (oldCode != null) {
                verificationCodeRepository.delete(oldCode);
                project.setVerificationCode(null);
                tattooProjectService.save(project);
            }
        }
        VerificationCode code = generateVerificationCode(project);
        sendVerificationEmail(code);
        return verificationCodeRepository.save(code);
    }

    public VerificationCode generateVerificationCode(TattooProject project) {
        VerificationCode code = new VerificationCode();
        code.setCode(generateRandomCode());
        code.setExpiresAt(LocalDateTime.now().plus(VERIFICATION_CODE_VALIDITY));
        code.setTattooProject(project);
        return code;
    }

    public String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < code.capacity(); i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    public void sendVerificationEmail(@NotNull VerificationCode verificationCode) throws MessagingException, UnsupportedEncodingException {
        String artistDisplayName = verificationCode.getTattooProject().getTattooArtist().getDisplayName();

        Context context = new Context();
        context.setVariable("logoUrl", logoUrl);
        context.setVariable("artist", artistDisplayName);
        for (int i = 1; i <= CODE_LENGTH; i++) {
            context.setVariable("code" + i, verificationCode.getCode().charAt(i - 1));
        }
        String htmlContent = templateEngine.process("verification-email", context);

        InternetAddress from = new InternetAddress(inkviteEmailAddress, "Inkvite");
        InternetAddress to = new InternetAddress(verificationCode.getTattooProject().getTattooClient().getEmail());
        String subject = String.format(MAIL_SUBJECT, artistDisplayName);
        mailSender.sendMail(from, to, subject, htmlContent);
    }

    /**
     * Checks if the given verification code is valid
     * and if so, updates the project's status to 'VERIFIED'.
     * @param projectId ID of the TattooProject to verify
     * @param verificationCode Verification code to check against the project's verification code
     * @return The updated TattooProject with status set to VERIFIED
     */
    public TattooProject verify(@NotNull UUID projectId, @NotBlank String verificationCode) {
        TattooProject project = tattooProjectService.findById(projectId);
        if (project.getVerificationCode().getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredVerificationCodeException(project.getId());
        }
        if (!project.getVerificationCode().getCode().equals(verificationCode)) {
            throw new WrongVerificationCodeException(project.getId());
        }
        if (project.getStatus() == TattooProjectStatus.UNVERIFIED) {
            project.setStatus(TattooProjectStatus.VERIFIED);
        }
        else {
            throw new IllegalStateException("Cannot verify following status : " + project.getStatus());
        }
        return tattooProjectService.save(project);
    }

}
