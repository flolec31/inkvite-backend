package com.flolecinc.inkvitebackend.file;

import com.flolecinc.inkvitebackend.exceptions.files.S3UploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class S3FileManager implements FileManager<S3Object> {

    @Value("${s3.bucketName}")
    private String bucketName;

    @Value("${s3.fileUrlFormat}")
    private String fileUrlFormat;

    private final S3Client s3Client;

    @Override
    public String uploadFileToServer(String fileName, byte[] fileBytes, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();
        PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(fileBytes));
        handleResponseError(response);
        return String.format(fileUrlFormat, bucketName, fileName);
    }


    @Override
    public List<S3Object> getFilesFromServer() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        handleResponseError(response);
        return response.contents();
    }

    @Override
    public void deleteFileFromServer(S3Object file) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(file.key())
                .build();
        DeleteObjectResponse response = s3Client.deleteObject(deleteRequest);
        handleResponseError(response);
    }

    public void handleResponseError(S3Response response) {
        if (!response.sdkHttpResponse().isSuccessful()) {
            throw new S3UploadException(
                    response.sdkHttpResponse().statusText().orElse("Unknown error"),
                    response.sdkHttpResponse().statusCode()
            );
        }
    }

}
