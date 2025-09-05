package com.flolecinc.inkvitebackend.tattoos.requestforms;

import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtist;
import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtistService;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClient;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClientDTO;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClientService;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectDTO;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectService;
import com.flolecinc.inkvitebackend.tattoos.references.TattooReferenceDTO;
import com.flolecinc.inkvitebackend.tattoos.references.TattooReferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestFormServiceTest {

    @Mock
    private TattooArtistService tattooArtistService;

    @Mock
    private TattooClientService tattooClientService;

    @Mock
    private TattooProjectService tattooProjectService;

    @Mock
    private TattooReferenceService tattooReferenceService;

    @InjectMocks
    private RequestFormService requestFormService;

    private static final String ARTIST_USERNAME = "artist_username";

    @Test
    void handleRequestForm_nominal_servicesCalled() {
        // Given
        RequestFormDTO requestFormDTO = new RequestFormDTO();
        requestFormDTO.setIdentity(new TattooClientDTO());
        TattooProjectDTO tattooProjectDTO = new TattooProjectDTO();
        TattooReferenceDTO tattooReferenceDTO = new TattooReferenceDTO();
        tattooProjectDTO.setReferences(List.of(tattooReferenceDTO));
        requestFormDTO.setProjectDetails(tattooProjectDTO);
        when(tattooArtistService.retrieveTattooArtistFromUsername(ARTIST_USERNAME)).thenReturn(new TattooArtist());
        when(tattooClientService.saveClient(any())).thenReturn(new TattooClient());
        TattooProject tattooProject = new TattooProject();
        when(tattooProjectService.bindEntitiesAndSaveProject(any(), any(), any())).thenReturn(tattooProject);
        when(tattooProjectService.findById(tattooProject.getId())).thenReturn(tattooProject);

        // When
        TattooProject returnedProject = requestFormService.handleRequestForm(ARTIST_USERNAME, requestFormDTO);

        // Then
        verify(tattooReferenceService).bindReferencesToProjectAndSaveThem(any(), eq(tattooProject));
        assertEquals(tattooProject, returnedProject);
    }

}