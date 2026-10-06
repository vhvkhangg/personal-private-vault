package com.vhvkhangg.personalprivatevault.media.internal.application.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Result of downloading a media binary from object storage.
 *
 * @param inputStream stream of binary data
 * @param sizeBytes byte size of object if known, or null
 * @param contentType MIME type of object if known, or null
 */
public record StorageDownloadResult(
        InputStream inputStream,
        Long sizeBytes,
        String contentType
) implements java.io.Closeable {

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
