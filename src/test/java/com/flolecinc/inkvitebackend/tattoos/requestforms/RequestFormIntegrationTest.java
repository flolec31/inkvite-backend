package com.flolecinc.inkvitebackend.tattoos.requestforms;

import com.flolecinc.inkvitebackend.config.AbstractIntegrationTest;
import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtist;
import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtistRepository;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClient;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClientRepository;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectRepository;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectStatus;
import com.flolecinc.inkvitebackend.tattoos.references.TattooReference;
import com.flolecinc.inkvitebackend.tattoos.references.TattooReferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestFormIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TattooArtistRepository tattooArtistRepository;

    @Autowired
    private TattooClientRepository tattooClientRepository;

    @Autowired
    private TattooProjectRepository tattooProjectRepository;

    @Autowired
    private TattooReferenceRepository tattooReferenceRepository;

    private TattooArtist artist;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        tattooClientRepository.deleteAll();
        tattooProjectRepository.deleteAll();
        tattooReferenceRepository.deleteAll();
        tattooArtistRepository.deleteAll();
        // Save an artist
        TattooArtist newArtist = new TattooArtist("andrea_g", "Andrea G.");
        artist = tattooArtistRepository.save(newArtist);
        // Handle new request form
        String jsonBody = IOUtils.toString(new ClassPathResource("request-form.json").getInputStream(), StandardCharsets.UTF_8);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/tattoos/requests/andrea_g")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));
    }

    @Test
    void handleNewRequestForm_nominal_tattooProjectSaved() {
        assertEquals(1, tattooClientRepository.count());
        TattooClient client = tattooClientRepository.findAll().get(0);
        assertEquals("John", client.getFirstName());
        assertEquals("Doe", client.getLastName());
        assertEquals("john.doe@aol.com", client.getEmail());

        assertEquals(1, tattooProjectRepository.count());
        TattooProject project = tattooProjectRepository.findAll().get(0);
        assertEquals("Japonese dragon", project.getProjectDescription());
        assertEquals("Left shoulder", project.getBodyPart());
        assertEquals(LocalDate.of(2025, 5, 18), project.getDesiredDate());
        assertEquals(TattooProjectStatus.UNVERIFIED, project.getStatus());
        assertEquals(artist.getId(), project.getTattooArtist().getId());
        assertEquals(client.getId(), project.getTattooClient().getId());

        assertEquals(2, tattooReferenceRepository.count());
        TattooReference reference1 = tattooReferenceRepository.findAll().get(0);
        assertEquals("image1.jpg", reference1.getImagePath());
        assertEquals("J'aime la forme", reference1.getComment());
        assertEquals(project.getId(), reference1.getTattooProject().getId());
        TattooReference reference2 = tattooReferenceRepository.findAll().get(1);
        assertEquals("image2.jpg", reference2.getImagePath());
        assertEquals("Je n'aime pas la couleur", reference2.getComment());
        assertEquals(project.getId(), reference2.getTattooProject().getId());
    }

    @Test
    void deletingArtistShouldDeleteProjectAndReferences() {
        // When
        tattooArtistRepository.deleteAll();

        // Then
        assertEquals(0, tattooArtistRepository.count());
        assertEquals(1, tattooClientRepository.count());
        assertEquals(0, tattooProjectRepository.count());
        assertEquals(0, tattooReferenceRepository.count());
    }

    @Test
    void deletingClientShouldDeleteProjectAndReferences() {
        // When
        tattooClientRepository.deleteAll();

        // Then
        assertEquals(1, tattooArtistRepository.count());
        assertEquals(0, tattooClientRepository.count());
        assertEquals(0, tattooProjectRepository.count());
        assertEquals(0, tattooReferenceRepository.count());
    }

    @Test
    void deletingProjectShouldDeleteReferences() {
        // When
        tattooProjectRepository.deleteAll();

        // Then
        assertEquals(1, tattooArtistRepository.count());
        assertEquals(1, tattooClientRepository.count());
        assertEquals(0, tattooProjectRepository.count());
        assertEquals(0, tattooReferenceRepository.count());
    }

    @Test
    void deletingReferencesShouldNotDeleteProject() {
        // When
        tattooReferenceRepository.deleteAll();

        // Then
        assertEquals(1, tattooArtistRepository.count());
        assertEquals(1, tattooClientRepository.count());
        assertEquals(1, tattooProjectRepository.count());
        assertEquals(0, tattooReferenceRepository.count());
    }

}