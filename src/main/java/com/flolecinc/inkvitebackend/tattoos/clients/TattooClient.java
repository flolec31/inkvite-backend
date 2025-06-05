package com.flolecinc.inkvitebackend.tattoos.clients;

import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tattoo_clients", schema = "public")
@Data
@NoArgsConstructor
public class TattooClient {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @OneToMany(mappedBy = "tattooClient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TattooProject> projects;

    public TattooClient(@Valid @NotNull TattooClientDTO tattooClientDTO) {
        this.firstName = tattooClientDTO.getFirstName();
        this.lastName = tattooClientDTO.getLastName();
        this.email = tattooClientDTO.getEmail();
    }

    public TattooClient(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

}
