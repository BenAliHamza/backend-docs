package tn.esprit.docsbackend.services;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for uploading images to an external storage provider
 * and returning a public URL.
 */
public interface ImageStorageService {

    /**
     * Uploads the given image and returns a public HTTP(S) URL.
     *
     * @param file          the image file to upload
     * @param filenameHint  optional hint for naming on the remote side
     * @return public URL of the uploaded image
     */
    String uploadImage(MultipartFile file, String filenameHint);
}
