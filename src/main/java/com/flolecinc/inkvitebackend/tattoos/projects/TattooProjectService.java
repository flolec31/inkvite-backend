package com.flolecinc.inkvitebackend.tattoos.projects;

import com.flolecinc.inkvitebackend.exceptions.notfound.TattooProjectNotFoundException;
import com.flolecinc.inkvitebackend.tattoos.artists.TattooArtist;
import com.flolecinc.inkvitebackend.tattoos.clients.TattooClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class TattooProjectService {

    private TattooProjectRepository tattooProjectRepository;

    @Transactional
    public TattooProject bindEntitiesAndSaveProject(TattooProject tattooProject,
                                                    TattooArtist tattooArtist,
                                                    TattooClient tattooClient) {
        tattooProject.setTattooArtist(tattooArtist);
        tattooProject.setTattooClient(tattooClient);
        return tattooProjectRepository.save(tattooProject);
    }

    public TattooProject findById(UUID id) {
        return tattooProjectRepository.findById(id).orElseThrow(() ->
            new TattooProjectNotFoundException(id));
    }

    @Transactional
    public TattooProject save(TattooProject project) {
        return tattooProjectRepository.save(project);
    }

}
