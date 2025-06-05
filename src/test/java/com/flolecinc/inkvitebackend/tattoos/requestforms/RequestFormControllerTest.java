package com.flolecinc.inkvitebackend.tattoos.requestforms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flolecinc.inkvitebackend.exceptions.notfound.TattooArtistNotFoundException;
import com.flolecinc.inkvitebackend.tattoos.projects.TattooProjectDTO;
import com.flolecinc.inkvitebackend.tattoos.references.TattooReferenceDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestFormController.class)
class RequestFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequestFormService requestFormService;

    private static final String ARTIST_USERNAME = "artist_username";

    private static String jsonBody;

    @BeforeAll
    static void setUp() throws IOException {
        jsonBody = IOUtils.toString(new ClassPathResource("request-form.json").getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void postNewTattooRequest_nominal_serviceCalledAndStatusOk() throws Exception {
        // Given
        ArgumentCaptor<RequestFormDTO> requestFormCaptor = ArgumentCaptor.forClass(RequestFormDTO.class);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/tattoos/requests/{tattooArtistUsername}", ARTIST_USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated());
        verify(requestFormService).handleRequestForm(eq(ARTIST_USERNAME), requestFormCaptor.capture());
        RequestFormDTO requestForm = requestFormCaptor.getValue();
        assertEquals("John", requestForm.getIdentity().getFirstName());
        assertEquals("Doe", requestForm.getIdentity().getLastName());
        assertEquals("john.doe@aol.com", requestForm.getIdentity().getEmail());
        assertEquals("Japonese dragon", requestForm.getProjectDetails().getProjectDescription());
        assertEquals("Left shoulder", requestForm.getProjectDetails().getBodyPart());
        assertEquals("2025-05-18", requestForm.getProjectDetails().getDesiredDate().toString());
        assertEquals(2, requestForm.getProjectDetails().getReferences().size());
        assertEquals("image1.jpg", requestForm.getProjectDetails().getReferences().get(0).getImagePath());
        assertEquals("J'aime la forme", requestForm.getProjectDetails().getReferences().get(0).getComment());
        assertEquals("image2.jpg", requestForm.getProjectDetails().getReferences().get(1).getImagePath());
        assertEquals("Je n'aime pas la couleur", requestForm.getProjectDetails().getReferences().get(1).getComment());
    }

    @Test
    void postNewTattooRequest_unknownArtist_exceptionHandledAndStatusNotFound() throws Exception {
        // Given
        doThrow(new TattooArtistNotFoundException(ARTIST_USERNAME)).when(requestFormService).handleRequestForm(eq(ARTIST_USERNAME), any());

        // When & Then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/tattoos/requests/{tattooArtistUsername}", ARTIST_USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals("{\"error\":\"Tattoo artist with username 'artist_username' not found\"}", result.getResponse().getContentAsString());
        verify(requestFormService).handleRequestForm(eq(ARTIST_USERNAME), any());
    }

    @Test
    void postNewTattooRequest_jsonParseError_exceptionHandledAndStatusBadRequest() throws Exception {
        // Given
        String badJson = "not a json";
        // When & Then
        invalidRequestHelper(badJson, "JSON parse error: Unrecognized token 'not'");
    }

    @Test
    void postNewTattooRequest_emptyJson_exceptionHandledAndStatusBadRequest() throws Exception {
        // Given
        String badJson = "";
        // When & Then
        invalidRequestHelper(badJson, "Required request body is missing");
    }

    @Test
    void postNewTattooRequest_invalidEmail_exceptionHandledAndStatusBadRequest() throws Exception {
        // Given
        String badJson = jsonBody.replace("john.doe@aol.com", "invalid-email");
        // When & Then
        invalidRequestHelper(badJson, "{\"identity.email\":\"must be a well-formed email address\"}");
    }

    @Test
    void postNewTattooRequest_nullRootFields_exceptionHandledAndStatusBadRequest() throws Exception {
        // Given
        String badJson = objectMapper.writeValueAsString(new RequestFormDTO());

        // When & Then
        invalidRequestHelper(badJson, "\"identity\":\"must not be null\"", "\"projectDetails\":\"must not be null\"");
    }

    @Test
    void postNewTattooRequest_blankIdentityFields_exceptionHandledAndStatusBadRequest() throws Exception {
        // Given
        String badJson = jsonBody
                .replace("John", "")
                .replace("Doe", "")
                .replace("john.doe@aol.com", "");

        // When & Then
        invalidRequestHelper(badJson,
                "\"identity.lastName\":\"must not be blank\"",
                "\"identity.email\":\"must not be blank\"",
                "\"identity.firstName\":\"must not be blank\"");
    }

    @Test
    void postNewTattooRequest_nullProjectDetailsFields_exceptionHandledAndStatusBadRequest() throws Exception {
        // Given
        RequestFormDTO requestFormDTO = objectMapper.readValue(jsonBody, RequestFormDTO.class);
        requestFormDTO.setProjectDetails(new TattooProjectDTO());
        String badJson = objectMapper.writeValueAsString(requestFormDTO);

        // When & Then
        invalidRequestHelper(badJson,
                "\"projectDetails.projectDescription\":\"must not be blank\"",
                "\"projectDetails.references\":\"must not be empty\"",
                "\"projectDetails.desiredDate\":\"must not be null\"",
                "\"projectDetails.bodyPart\":\"must not be blank\"");
    }

    @Test
    void postNewTattooRequest_blankReferenceFields_exceptionHandledAndStatusBadRequest() throws Exception {
        // Given
        RequestFormDTO requestFormDTO = objectMapper.readValue(jsonBody, RequestFormDTO.class);
        requestFormDTO.getProjectDetails().setReferences(List.of(new TattooReferenceDTO()));
        String badJson = objectMapper.writeValueAsString(requestFormDTO);
        // When & Then
        invalidRequestHelper(badJson, "{\"projectDetails.references[0].imagePath\":\"must not be blank\"");
    }

    @Test
    void postNewTattooRequest_impossibleDate_exceptionHandledAndStatusBadRequest() throws Exception {
        // Given
        String badJson = jsonBody.replace("2025-05-18", "2025-05-32");
        // When & Then
        invalidRequestHelper(badJson, "JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String \\\"2025-05-32\\\"");
    }

    @Test
    void postNewTattooRequest_invalidDateFormat_exceptionHandledAndStatusBadRequest() throws Exception {
        // Given
        String badJson = jsonBody.replace("2025-05-18", "invalid-date");
        // When & Then
        invalidRequestHelper(badJson, "JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String \\\"invalid-date\\\"");
    }

    private void invalidRequestHelper(String badJson, String... messages) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/tattoos/requests/{tattooArtistId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        for (String message : messages) {
            assertTrue(body.contains(message));
        }
    }

}