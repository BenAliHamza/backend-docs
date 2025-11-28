package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.dto.notification.NotificationDto;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.Notification;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.NotificationType;
import tn.esprit.docsbackend.mappers.NotificationMapper;
import tn.esprit.docsbackend.repositories.NotificationRepository;
import tn.esprit.docsbackend.repositories.UserRepository;
import tn.esprit.docsbackend.services.NotificationService;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendAppointmentNotification(
            Long targetUserId,
            NotificationType type,
            Appointment appointment,
            String overrideTitle,
            String overrideMessage
    ) {
        if (targetUserId == null || type == null || appointment == null) {
            return;
        }

        User targetUser = userRepository.findById(targetUserId)
                .filter(u -> !u.isDeleted())
                .orElse(null);

        if (targetUser == null) {
            return;
        }

        String title = (overrideTitle != null) ? overrideTitle : type.name();
        String message = (overrideMessage != null)
                ? overrideMessage
                : "Notification about appointment " + appointment.getId();

        Notification notification = Notification.builder()
                .targetUser(targetUser)
                .type(type)
                .title(title)
                .message(message)
                .appointmentId(appointment.getId())
                .read(false)
                .readAt(null)
                .build();

        Notification saved = notificationRepository.save(notification);

        NotificationDto dto = notificationMapper.toDto(saved);

        // Real-time push to user
        messagingTemplate.convertAndSend(
                "/topic/users/" + targetUserId + "/appointments",
                dto
        );
    }
}
