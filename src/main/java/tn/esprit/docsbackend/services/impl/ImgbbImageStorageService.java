package tn.esprit.docsbackend.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.docsbackend.config.ImageStorageProperties;
import tn.esprit.docsbackend.services.ImageStorageService;

import java.io.IOException;
import java.util.Base64;

/**
 * ImageStorageService implementation that uploads images to ImgBB.
 *
 * Docs: https://api.imgbb.com/
 *
 * Uses properties from ImageStorageProperties (image-storage.imgbb.*).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImgbbImageStorageService implements ImageStorageService {

    private final ImageStorageProperties properties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String uploadImage(MultipartFile file, String filenameHint) {
        String apiKey = properties.getApiKey();
        String apiUrl = properties.getApiUrl();

        if (apiKey == null || apiKey.isBlank()) {
            log.error("ImgBB API key is not configured");
            throw new IllegalStateException("Image hosting is not configured on the server");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        try {
            // ImgBB accepts "image" as base64 string in form-data.
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "key=" + apiKey + "&image=" + base64;

            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity =
                    restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                log.error("ImgBB upload failed with status {} and body {}",
                        responseEntity.getStatusCode(), responseEntity.getBody());
                throw new IllegalStateException("Failed to upload image");
            }

            String responseBody = responseEntity.getBody();
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode dataNode = root.path("data");
            JsonNode urlNode = dataNode.path("url");

            if (urlNode.isMissingNode() || urlNode.asText().isBlank()) {
                log.error("ImgBB response missing URL: {}", responseBody);
                throw new IllegalStateException("Image upload response did not contain URL");
            }

            return urlNode.asText();
        } catch (IOException | RestClientException e) {
            log.error("Error while uploading image to ImgBB", e);
            throw new IllegalStateException("Failed to upload image", e);
        }
    }
}
