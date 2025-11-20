package tn.esprit.docsbackend.utils.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Utility component to load JSON seed data from the classpath.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedJsonLoader {

    private final ObjectMapper objectMapper;

    /**
     * Load a JSON array from the given classpath resource into a list of the target type.
     *
     * @param resourcePath path relative to classpath root (e.g. "seed/specialties.json")
     * @param typeRef      Jackson TypeReference for the target list element type
     * @param <T>          element type
     * @return list of parsed objects or empty list on error
     */
    public <T> List<T> loadList(String resourcePath, TypeReference<List<T>> typeRef) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                log.warn("SeedJsonLoader: resource {} not found on classpath", resourcePath);
                return Collections.emptyList();
            }

            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readValue(is, typeRef);
            }
        } catch (Exception e) {
            log.error("SeedJsonLoader: failed to load JSON list from {}: {}", resourcePath, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
