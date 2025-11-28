package tn.esprit.docsbackend.notifications;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.enums.NotificationType;

@Component
public class NotificationMessageFactory {

    public record MessagePair(String title, String message) {}

    public MessagePair fallback(NotificationType type, Appointment appt) {
        String title = type != null ? type.name() : "NOTIFICATION";
        String message = (appt != null)
                ? "Notification about appointment " + appt.getId()
                : "Notification";
        return new MessagePair(title, message);
    }
}
