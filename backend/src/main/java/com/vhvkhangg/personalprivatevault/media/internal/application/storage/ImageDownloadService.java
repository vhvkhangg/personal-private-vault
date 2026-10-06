package com.vhvkhangg.personalprivatevault.media.internal.application.storage;

import com.vhvkhangg.personalprivatevault.media.image.ImageNotFoundException;
import com.vhvkhangg.personalprivatevault.media.image.ImageOperations;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageDownloadService {

    private final ImageOperations imageOperations;
    private final MediaStoragePort mediaStoragePort;

    public ImageBinaryDownload downloadImage(Long id) {
        ImageView image = imageOperations.findById(id);
        if (image == null) {
            throw new ImageNotFoundException(id);
        }

        String objectKey = image.objectKey();
        if (objectKey == null || objectKey.isBlank()) {
            throw new StorageIntegrityException("Image metadata has no object key");
        }

        StorageDownloadResult result = mediaStoragePort.download(objectKey);
        return new ImageBinaryDownload(image, result);
    }

    public record ImageBinaryDownload(
            ImageView metadata,
            StorageDownloadResult downloadResult
    ) implements java.io.Closeable {
        @Override
        public void close() throws java.io.IOException {
            if (downloadResult != null) {
                downloadResult.close();
            }
        }
    }
}
