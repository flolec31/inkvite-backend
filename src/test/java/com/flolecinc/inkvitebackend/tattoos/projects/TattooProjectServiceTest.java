package com.flolecinc.inkvitebackend.tattoos.projects;

import com.flolecinc.inkvitebackend.exceptions.notfound.TattooProjectNotFoundException;
import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtist;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TattooProjectServiceTest {

    @Mock
    private TattooProjectRepository tattooProjectRepository;

    @InjectMocks
    private TattooProjectService tattooProjectService;

    @Test
    void bindEntitiesAndSaveProject_nominal_repositoryCalled() {
        // Given
        TattooProject project = new TattooProject();
        TattooArtist artist = new TattooArtist();
        TattooClient client = new TattooClient();

        // When
        tattooProjectService.bindEntitiesAndSaveProject(project, artist, client);

        // Then
        verify(tattooProjectRepository).save(project);
        assertEquals(artist, project.getTattooArtist());
        assertEquals(client, project.getTattooClient());
    }

    @Test
    void findById_nominal_repositoryCalled() {
        // Given
        UUID id = UUID.randomUUID();
        TattooProject project = new TattooProject();
        when(tattooProjectRepository.findById(id)).thenReturn(Optional.of(project));

        // When
        TattooProject result = tattooProjectService.findById(id);

        // Then
        verify(tattooProjectRepository).findById(id);
        assertEquals(project, result);
    }

    @Test
    void findById_projectNotFound_exceptionThrown() {
        // Given
        UUID id = UUID.randomUUID();
        when(tattooProjectRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        Exception e = assertThrows(TattooProjectNotFoundException.class, () -> tattooProjectService.findById(id));
        assertEquals("Tattoo project with ID '" + id + "' not found", e.getMessage());
    }

    @Test
    void save_nominal_repositoryCalled() {
        // Given
        TattooProject project1 = new TattooProject();
        TattooProject project2 = new TattooProject();
        when(tattooProjectRepository.save(project1)).thenReturn(project2);

        // When
        TattooProject project3 = tattooProjectService.save(project1);

        // Then
        verify(tattooProjectRepository).save(project1);
        assertEquals(project2, project3);
    }

}