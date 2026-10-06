package com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage;

import com.vhvkhangg.personalprivatevault.media.internal.application.storage.MediaStoragePort;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageDownloadResult;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageIntegrityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class S3MediaStorageAdapter implements MediaStoragePort {

    private final S3Client s3Client;
    private final MediaStorageProperties properties;

    @Override
    public void upload(String objectKey, InputStream inputStream, long sizeBytes, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentLength(sizeBytes)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, sizeBytes));
        } catch (SdkException e) {
            log.error("Failed to upload object to storage provider");
            throw new RuntimeException("Storage upload failed", e);
        }
    }

    @Override
    public StorageDownloadResult download(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(request);
            GetObjectResponse response = responseStream.response();
            return new StorageDownloadResult(responseStream, response.contentLength(), response.contentType());
        } catch (NoSuchKeyException e) {
            log.warn("Object missing in storage during download");
            throw new StorageIntegrityException("Binary object not found in storage");
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.warn("Object missing in storage (404) during download");
                throw new StorageIntegrityException("Binary object not found in storage");
            }
            log.error("Storage provider returned error during download");
            throw new RuntimeException("Storage download failed", e);
        } catch (SdkException e) {
            log.error("Failed to download object from storage provider");
            throw new RuntimeException("Storage download failed", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(request);
        } catch (SdkException e) {
            log.error("Failed to delete object from storage provider");
            throw new RuntimeException("Storage delete failed", e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            log.error("Storage provider error checking object existence");
            return false;
        } catch (SdkException e) {
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(properties.getBucket())
                    .build();

            s3Client.headBucket(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
