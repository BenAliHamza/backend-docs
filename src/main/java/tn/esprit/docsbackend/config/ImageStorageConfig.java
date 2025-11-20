package tn.esprit.docsbackend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable binding of image storage properties.
 */
@Configuration
@EnableConfigurationProperties(ImageStorageProperties.class)
public class ImageStorageConfig {
    // No code needed here; the annotation is enough to register the properties bean.
}
