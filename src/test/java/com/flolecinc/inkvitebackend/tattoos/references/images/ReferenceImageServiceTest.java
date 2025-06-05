package com.flolecinc.inkvitebackend.tattoos.references.images;

import com.flolecinc.inkvitebackend.exceptions.files.FileReaderException;
import com.flolecinc.inkvitebackend.exceptions.files.UnsupportedImageTypeException;
import com.flolecinc.inkvitebackend.file.FileManager;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferenceImageServiceTest {

    public static final String S3_IMAGE_URL = "https://s3.supabase.com/bucket/image.jpg";
    public static final byte[] MOCK_IMAGE_BYTES = new byte[]{1, 2, 3};
    public static final MockMultipartFile MOCK_MULTIPART_FILE = new MockMultipartFile("file", "image.jpg", "image/jpeg", MOCK_IMAGE_BYTES);

    @Mock
    private FileManager<S3Object> fileManager;

    @Mock
    private Tika tika;

    @InjectMocks
    private ReferenceImageService referenceImageService;

    @Test
    void uploadImageFromDevice_nominal() {
        // Given
        when(tika.detect(MOCK_IMAGE_BYTES)).thenReturn("image/jpeg");
        when(fileManager.uploadFileToServer(anyString(), eq(MOCK_IMAGE_BYTES), eq("image/jpeg"))).thenReturn(S3_IMAGE_URL);

        // When
        TempImageDTO result = referenceImageService.uploadImageFromDevice(MOCK_MULTIPART_FILE);

        // Then
        assertNotNull(result);
        assertNotNull(result.imagePath());
        assertEquals(S3_IMAGE_URL, result.imageUrl());
    }

    @Test
    void uploadImageFromDevice_imageUploadException() throws IOException {
        // Given
        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.getBytes()).thenThrow(new IOException());
        when(badFile.getOriginalFilename()).thenReturn("image.jpg");

        // When & Then
        FileReaderException exception = assertThrows(FileReaderException.class, () -> referenceImageService.uploadImageFromDevice(badFile));
        assertEquals("Failed to read uploaded file: image.jpg", exception.getMessage());
    }

    @Test
    void uploadImage_unsupportedImageTypeException() {
        // Given
        when(tika.detect(MOCK_IMAGE_BYTES)).thenReturn("text/plain");

        // When & Then
        UnsupportedImageTypeException exception = assertThrows(UnsupportedImageTypeException.class, () -> referenceImageService.uploadImage(MOCK_IMAGE_BYTES));
        assertEquals("Unsupported image type: text/plain", exception.getMessage());
    }

    @Test
    void cleanTempImagesFolder_nominal() {
        // Given
        ReflectionTestUtils.setField(referenceImageService, "tempBucketMaxHours", 24);
        S3Object s3ObjectOld1 = S3Object.builder()
                .lastModified(Instant.now().minus(Duration.ofHours(26)))
                .build();
        S3Object s3ObjectOld2 = S3Object.builder()
                .lastModified(Instant.now().minus(Duration.ofHours(25)))
                .build();
        S3Object s3ObjectNew = S3Object.builder()
                .lastModified(Instant.now().minus(Duration.ofHours(23)))
                .build();
        when(fileManager.getFilesFromServer()).thenReturn(List.of(s3ObjectOld1, s3ObjectOld2, s3ObjectNew));

        // When
        int deletedCount = referenceImageService.cleanTempImagesFolder();

        // Then
        assertEquals(2, deletedCount);
        verify(fileManager).deleteFileFromServer(s3ObjectOld1);
        verify(fileManager).deleteFileFromServer(s3ObjectOld2);
        verify(fileManager, times(0)).deleteFileFromServer(s3ObjectNew);
    }

}