package com.flolecinc.inkvitebackend.security.verificationcode;

import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RestController
@RequestMapping("/api/security/verification-code")
@AllArgsConstructor
public class VerificationCodeController {

    private final VerificationCodeService verificationCodeService;

    @PostMapping("/send")
    public ResponseEntity<VerificationCode> sendVerificationEmail(@RequestParam UUID projectId) throws MessagingException, UnsupportedEncodingException {
        VerificationCode verificationCode = verificationCodeService.generateAndSendVerificationCode(projectId, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationCode);
    }

    @PostMapping("/resend")
    public ResponseEntity<VerificationCode> resendVerificationEmail(@RequestParam UUID projectId) throws MessagingException, UnsupportedEncodingException {
        VerificationCode verificationCode = verificationCodeService.generateAndSendVerificationCode(projectId, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(verificationCode);
    }

    @PostMapping("/verify")
    public ResponseEntity<TattooProject> verifyProject(@RequestParam UUID projectId, @RequestParam String verificationCode) {
        return ResponseEntity.ok(verificationCodeService.verify(projectId, verificationCode));
    }

}
