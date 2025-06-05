package com.flolecinc.inkvitebackend.tattoos.artists;

import com.flolecinc.inkvitebackend.exceptions.notfound.TattooArtistNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TattooArtistServiceTest {

    @Mock
    private TattooArtistRepository tattooArtistRepository;

    @InjectMocks
    private TattooArtistService tattooArtistService;

    private static final String ARTIST_USERNAME = "artist_username";

    @Test
    void retrieveTattooArtist_artistExists_artistReturned() {
        // Given
        TattooArtist artist = new TattooArtist();
        when(tattooArtistRepository.findByUsername(ARTIST_USERNAME)).thenReturn(Optional.of(artist));

        // When
        TattooArtist result = tattooArtistService.retrieveTattooArtistFromUsername(ARTIST_USERNAME);

        // Then
        assertEquals(artist, result);
        verify(tattooArtistRepository).findByUsername(ARTIST_USERNAME);
    }

    @Test
    void retrieveTattooArtist_artistDoesntExist_emptyReturned() {
        // Given
        when(tattooArtistRepository.findByUsername(ARTIST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TattooArtistNotFoundException.class, () -> tattooArtistService.retrieveTattooArtistFromUsername(ARTIST_USERNAME));
        verify(tattooArtistRepository).findByUsername(ARTIST_USERNAME);
    }

}