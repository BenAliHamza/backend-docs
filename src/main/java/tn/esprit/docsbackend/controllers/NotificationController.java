package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.common.ListResponse;
import tn.esprit.docsbackend.dto.notification.NotificationDto;
import tn.esprit.docsbackend.entities.Notification;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.mappers.NotificationMapper;
import tn.esprit.docsbackend.repositories.NotificationRepository;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @GetMapping("/me")
    public ResponseEntity<ListResponse<NotificationDto>> getMyNotifications() {

        User currentUser = SecurityUtils.getCurrentUserOrThrow();
        Long userId = currentUser.getId();

        List<Notification> notifications = notificationRepository
                .findTop20ByTargetUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);

        List<NotificationDto> list = notifications.stream()
                .map(notificationMapper::toDto)
                .toList();

        return ResponseEntity.ok(ListResponse.of(list));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {

        User currentUser = SecurityUtils.getCurrentUserOrThrow();
        Long userId = currentUser.getId();

        Notification n = notificationRepository.findById(id)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!n.getTargetUser().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        n.setRead(true);
        n.setReadAt(java.time.LocalDateTime.now());
        notificationRepository.save(n);

        return ResponseEntity.noContent().build();
    }
}
