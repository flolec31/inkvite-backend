package com.flolecinc.inkvitebackend.tattoos.artists;

import com.flolecinc.inkvitebackend.exceptions.notfound.TattooArtistNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TattooArtistService {

    private TattooArtistRepository tattooArtistRepository;

    public TattooArtist retrieveTattooArtistFromUsername(String tattooArtistUsername) {
        return tattooArtistRepository.findByUsername(tattooArtistUsername).orElseThrow(
                () -> new TattooArtistNotFoundException(tattooArtistUsername)
        );
    }

}
