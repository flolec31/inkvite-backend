package com.flolecinc.inkvitebackend.exceptions.notfound;

import java.util.UUID;

public class TattooProjectNotFoundException extends RessourceNotFoundException {

    public TattooProjectNotFoundException(UUID projectId) {
        super("Tattoo project with ID '" + projectId + "' not found");
    }

}
