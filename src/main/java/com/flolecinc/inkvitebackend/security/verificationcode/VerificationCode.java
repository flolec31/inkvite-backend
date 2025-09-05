package com.flolecinc.inkvitebackend.security.verificationcode;

import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_codes", schema = "public")
@Data
@NoArgsConstructor
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    @OneToOne
    @JoinColumn(name = "tattoo_project_id", nullable = false)
    private TattooProject tattooProject;

    public VerificationCode(String code, LocalDateTime expiresAt, TattooProject tattooProject) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.tattooProject = tattooProject;
    }

}
