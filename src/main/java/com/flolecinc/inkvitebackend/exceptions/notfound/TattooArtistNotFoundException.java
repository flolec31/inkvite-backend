package com.flolecinc.inkvitebackend.exceptions.notfound;

public class TattooArtistNotFoundException extends RessourceNotFoundException {

    public TattooArtistNotFoundException(String username) {
        super("Tattoo artist with username '" + username + "' not found");
    }

}
