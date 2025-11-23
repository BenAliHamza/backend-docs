package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.services.ImageStorageService;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImgbbImageStorageService implements ImageStorageService {

    @Value("${image-storage.imgbb.api-key}")
    private String apiKey;

    /**
     * Use the same key name as in application.yml: image-storage.imgbb.api-url
     */
    @Value("${image-storage.imgbb.api-url:https://api.imgbb.com/1/upload}")
    private String uploadUrl;

    // Simple RestTemplate – for real projects you might inject a @Bean
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No image file provided");
        }

        try {
            byte[] bytes = file.getBytes();

            // We send the *file* as multipart, not a base64 string.
            ByteArrayResource imageResource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null
                            ? file.getOriginalFilename()
                            : "upload-image";
                }
            };

            var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
            body.add("key", apiKey);
            body.add("image", imageResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

            log.info("ImgbbImageStorageService: uploading image to ImgBB (size={} bytes, url={})",
                    bytes.length, uploadUrl);

            ResponseEntity<Map> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("ImgbbImageStorageService: non-success response from ImgBB: status={}, body={}",
                        response.getStatusCode(), response.getBody());
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Image hosting service returned an error"
                );
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            if (data == null || !data.containsKey("url")) {
                log.error("ImgbbImageStorageService: ImgBB response missing 'data.url': {}", response.getBody());
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Image hosting response did not contain image URL"
                );
            }

            String imageUrl = data.get("url").toString();
            log.info("ImgbbImageStorageService: uploaded image successfully -> {}", imageUrl);
            return imageUrl;

        } catch (HttpClientErrorException e) {
            // This is where "Invalid base64 string" was being logged
            log.error("ImgbbImageStorageService: HTTP error from ImgBB: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Image hosting service rejected the image"
            );
        } catch (IOException e) {
            log.error("ImgbbImageStorageService: failed to read uploaded file bytes", e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Could not read image file"
            );
        } catch (Exception e) {
            log.error("ImgbbImageStorageService: unexpected error while uploading image", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error while uploading image"
            );
        }
    }
}
