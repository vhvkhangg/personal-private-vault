package com.vhvkhangg.personalprivatevault.media.internal.application.storage;

import java.io.InputStream;

/**
 * Storage port owned by Media module for binary object operations.
 * Isolates all object-storage provider SDKs from application and web layers.
 */
public interface MediaStoragePort {

    /**
     * Upload binary data from stream into object storage.
     *
     * @param objectKey server-owned opaque key
     * @param inputStream stream providing binary content
     * @param sizeBytes byte size
     * @param contentType MIME type
     */
    void upload(String objectKey, InputStream inputStream, long sizeBytes, String contentType);

    /**
     * Download binary data from object storage.
     *
     * @param objectKey server-owned opaque key
     * @return download result containing stream and metadata
     */
    StorageDownloadResult download(String objectKey);

    /**
     * Delete an object from storage (used during compensation cleanup).
     *
     * @param objectKey server-owned opaque key
     */
    void delete(String objectKey);

    /**
     * Check if an object exists in storage.
     *
     * @param objectKey server-owned opaque key
     * @return true if object exists, false otherwise
     */
    boolean exists(String objectKey);

    /**
     * Check if storage service/bucket is available.
     *
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}
