package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.dto.user.UserDto;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.mappers.UserMapper;
import tn.esprit.docsbackend.services.UserService;
import tn.esprit.docsbackend.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        return userMapper.toDto(user);
    }
}
