package com.flolecinc.inkvitebackend.tattoos.references.images;

import com.flolecinc.inkvitebackend.exceptions.files.FileReaderException;
import com.flolecinc.inkvitebackend.exceptions.files.S3UploadException;
import com.flolecinc.inkvitebackend.exceptions.files.UnsupportedImageTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReferenceImageController.class)
class ReferenceImageControllerTest {

    public static final String S3_IMAGE_PATH = "image.jpg";
    public static final String S3_IMAGE_URL = "https://s3.supabase.com/bucket/image.jpg";
    public static final MockMultipartFile MOCK_MULTIPART_FILE = new MockMultipartFile("file", "image.jpg", "image/jpeg", "image content".getBytes());


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReferenceImageService referenceImageService;

    private TempImageDTO s3TempImage;

    @BeforeEach
    void setUp() {
        s3TempImage = new TempImageDTO(S3_IMAGE_PATH, S3_IMAGE_URL);
    }

    @Test
    void uploadImageFromDevice_nominal_serviceCalledAndStatusOk() throws Exception {
        // Given
        when(referenceImageService.uploadImageFromDevice(MOCK_MULTIPART_FILE)).thenReturn(s3TempImage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/references-images/upload-from-device")
                        .file(MOCK_MULTIPART_FILE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagePath").value(S3_IMAGE_PATH))
                .andExpect(jsonPath("$.imageUrl").value(S3_IMAGE_URL));
        verify(referenceImageService).uploadImageFromDevice(MOCK_MULTIPART_FILE);
    }

    @Test
    void uploadImage_errorWhileReadingFile_exceptionHandled() throws Exception {
        // Given
        when(referenceImageService.uploadImageFromDevice(MOCK_MULTIPART_FILE)).thenThrow(new FileReaderException("error while reading file", null));

        // When & Then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/references-images/upload-from-device")
                        .file(MOCK_MULTIPART_FILE))
                .andExpect(status().isInternalServerError())
                .andReturn();
        assertEquals("{\"error\":\"error while reading file\"}", result.getResponse().getContentAsString());
    }

    @Test
    void uploadImage_unsupportedImageType_exceptionHandled() throws Exception {
        // Given
        when(referenceImageService.uploadImageFromDevice(MOCK_MULTIPART_FILE)).thenThrow(new UnsupportedImageTypeException("text/plain"));

        // When & Then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/references-images/upload-from-device")
                        .file(MOCK_MULTIPART_FILE))
                .andExpect(status().isUnsupportedMediaType())
                .andReturn();
        assertEquals("{\"error\":\"Unsupported image type: text/plain\"}", result.getResponse().getContentAsString());
    }

    @Test
    void uploadImage_s3UploadError_exceptionHandled() throws Exception {
        // Given
        when(referenceImageService.uploadImageFromDevice(MOCK_MULTIPART_FILE)).thenThrow(new S3UploadException("teapot error", 418));

        // When & Then
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/references-images/upload-from-device")
                        .file(MOCK_MULTIPART_FILE))
                .andExpect(status().isIAmATeapot())
                .andReturn();
        assertEquals("{\"error\":\"Failed to upload a file to the S3 server: teapot error\"}", result.getResponse().getContentAsString());
    }

    @Test
    void cleanTempImagesFolder_nominal_serviceCalledAndStatusOk() throws Exception {
        // Given
        when(referenceImageService.cleanTempImagesFolder()).thenReturn(42);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/references-images/clean-temp-folder"))
                .andExpect(status().isOk())
                .andExpect(content().string("42 images deleted from temp folder"));
    }

    @Test
    void cleanTempImagesFolder_oneImageDeleted_serviceCalledAndStatusOk() throws Exception {
        // Given
        when(referenceImageService.cleanTempImagesFolder()).thenReturn(1);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/references-images/clean-temp-folder"))
                .andExpect(status().isOk())
                .andExpect(content().string("1 image deleted from temp folder"));
    }

}