package com.flolecinc.inkvitebackend.tattoos.references.images;

import com.flolecinc.inkvitebackend.exceptions.files.FileReaderException;
import com.flolecinc.inkvitebackend.exceptions.files.UnsupportedImageTypeException;
import com.flolecinc.inkvitebackend.file.FileManager;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferenceImageService {

    @Value("${s3.tempBucketMaxHours}")
    private int tempBucketMaxHours;

    private final FileManager<S3Object> fileManager;
    private final Tika tika;

    public TempImageDTO uploadImageFromDevice(MultipartFile file) {
        byte[] imageBytes;
        try {
            imageBytes = file.getBytes();
        } catch (IOException e) {
            throw new FileReaderException("Failed to read uploaded file: " + file.getOriginalFilename(), e);
        }
        return uploadImage(imageBytes);
    }

    public TempImageDTO uploadImage(byte[] imageBytes) {
        String imageName = String.valueOf(UUID.randomUUID());
        String contentType = tika.detect(imageBytes);
        if (!contentType.startsWith("image/")) {
            throw new UnsupportedImageTypeException(contentType);
        }
        String imageUrl = fileManager.uploadFileToServer(imageName, imageBytes, contentType);
        return new TempImageDTO(imageName, imageUrl);
    }

    @Scheduled(cron = "0 0 4 * * ?") // Every day at 4am
    public int cleanTempImagesFolder() {
        List<S3Object> allTempImages = fileManager.getFilesFromServer();
        Instant cutoffTime = Instant.now().minus(Duration.ofHours(tempBucketMaxHours));
        int deleted = 0;
        for (S3Object image : allTempImages) {
            if (image.lastModified().isBefore(cutoffTime)) {
                fileManager.deleteFileFromServer(image);
                deleted++;
            }
        }
        return deleted;
    }

}
