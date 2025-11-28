package tn.esprit.docsbackend.dto.notification;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Long appointmentId;
    private LocalDateTime createdAt;
    private boolean read;
}
