package com.flolecinc.inkvitebackend.tattoos.requestforms;

import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtist;
import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtistService;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClient;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClientService;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectService;
import com.flolecinc.inkvitebackend.tattoos.references.TattooReference;
import com.flolecinc.inkvitebackend.tattoos.references.TattooReferenceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class RequestFormService {

    private TattooArtistService tattooArtistService;
    private TattooClientService tattooClientService;
    private TattooProjectService tattooProjectService;
    private TattooReferenceService tattooReferenceService;

    /**
     * Saving a new tattoo project (and associated references) after receiving a request form.
     *
     * @param tattooArtistUsername The username of the desired artist
     * @param requestFormDTO The request form that the client filled and sent
     */
    @Transactional
    public TattooProject handleRequestForm(String tattooArtistUsername, RequestFormDTO requestFormDTO) {
        TattooArtist artist = tattooArtistService.retrieveTattooArtistFromUsername(tattooArtistUsername);

        TattooClient client = new TattooClient(requestFormDTO.getIdentity());
        client = tattooClientService.saveClient(client);

        TattooProject project = new TattooProject(requestFormDTO.getProjectDetails());
        project = tattooProjectService.bindEntitiesAndSaveProject(project, artist, client);

        List<TattooReference> references = requestFormDTO.getProjectDetails().getReferences()
                .stream()
                .map(TattooReference::new)
                .toList();
        tattooReferenceService.bindReferencesToProjectAndSaveThem(references, project);

        return tattooProjectService.findById(project.getId());
    }

}
