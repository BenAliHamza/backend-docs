package tn.esprit.docsbackend.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.dto.notification.NotificationDto;
import tn.esprit.docsbackend.entities.Notification;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .appointmentId(n.getAppointmentId())
                .createdAt(n.getCreatedAt())
                .read(n.getRead() != null && n.getRead())
                .build();
    }
}
