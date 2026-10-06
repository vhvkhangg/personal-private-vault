package com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage;

import com.vhvkhangg.personalprivatevault.media.internal.application.storage.MediaStoragePort;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageDisabledException;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageDownloadResult;

import java.io.InputStream;

public class DisabledMediaStorageAdapter implements MediaStoragePort {

    @Override
    public void upload(String objectKey, InputStream inputStream, long sizeBytes, String contentType) {
        throw new StorageDisabledException("Managed image storage is not enabled");
    }

    @Override
    public StorageDownloadResult download(String objectKey) {
        throw new StorageDisabledException("Managed image storage is not enabled");
    }

    @Override
    public void delete(String objectKey) {
        throw new StorageDisabledException("Managed image storage is not enabled");
    }

    @Override
    public boolean exists(String objectKey) {
        return false;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
