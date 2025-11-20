package tn.esprit.docsbackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds properties from:
 *
 * image-storage.imgbb.api-key
 * image-storage.imgbb.api-url
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "image-storage.imgbb")
public class ImageStorageProperties {

    /**
     * ImgBB API key.
     */
    private String apiKey;

    /**
     * ImgBB API URL.
     */
    private String apiUrl = "https://api.imgbb.com/1/upload";
}
