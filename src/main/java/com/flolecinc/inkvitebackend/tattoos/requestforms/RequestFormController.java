package com.flolecinc.inkvitebackend.tattoos.requestforms;

import com.flolecinc.inkvitebackend.tattoos.projects.TattooProject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tattoos/requests")
@AllArgsConstructor
public class RequestFormController {

    private RequestFormService requestFormService;

    @PostMapping("/{tattooArtistUsername}")
    public ResponseEntity<TattooProject> handleRequestForm(
            @PathVariable String tattooArtistUsername,
            @RequestBody @Valid RequestFormDTO requestForm) {
        TattooProject project = requestFormService.handleRequestForm(tattooArtistUsername, requestForm);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

}
