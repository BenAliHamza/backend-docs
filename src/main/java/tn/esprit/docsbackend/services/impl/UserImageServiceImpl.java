package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
    public UserDto uploadCurrentUserProfileImage(MultipartFile file) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        // Upload to ImgBB (or whatever implementation is behind ImageStorageService)
        String imageUrl = imageStorageService.uploadImage(file);

        // Persist on user
        currentUser.setProfileImage(imageUrl);
        User saved = userRepository.save(currentUser);

        // Return updated user as DTO
        return userMapper.toDto(saved);
    }
}
