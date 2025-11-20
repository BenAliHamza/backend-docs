package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.user.UserDto;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.mappers.UserMapper;
import tn.esprit.docsbackend.repositories.UserRepository;
import tn.esprit.docsbackend.services.ImageStorageService;
import tn.esprit.docsbackend.services.UserImageService;
import tn.esprit.docsbackend.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
public class UserImageServiceImpl implements UserImageService {

    private final ImageStorageService imageStorageService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto uploadCurrentUserProfileImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null ||
                !(contentType.equalsIgnoreCase("image/jpeg")
                        || contentType.equalsIgnoreCase("image/png")
                        || contentType.equalsIgnoreCase("image/jpg")
                        || contentType.equalsIgnoreCase("image/webp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported image type");
        }

        // Optionally enforce a max size (e.g. 5MB)
        long maxSizeBytes = 5L * 1024 * 1024;
        if (imageFile.getSize() > maxSizeBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is too large (max 5MB)");
        }

        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        // Upload image to external provider
        String filenameHint = "user_" + currentUser.getId();
        String imageUrl = imageStorageService.uploadImage(imageFile, filenameHint);

        // Store URL in user entity
        currentUser.setProfileImage(imageUrl);
        User saved = userRepository.save(currentUser);

        return userMapper.toDto(saved);
    }
}
