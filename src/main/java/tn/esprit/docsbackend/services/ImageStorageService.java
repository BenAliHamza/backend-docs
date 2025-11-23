package tn.esprit.docsbackend.services;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    /**
     * Upload the given image file to an external storage provider
     * and return the public URL of the stored image.
     */
    String uploadImage(MultipartFile file);
}
