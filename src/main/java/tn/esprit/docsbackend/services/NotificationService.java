package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.enums.NotificationType;

public interface NotificationService {

    /**
     * Send an appointment-related notification.
     *
     * @param targetUserId    the user who should receive the notification
     * @param type            notification type (APPOINTMENT_ACCEPTED, ...)
     * @param appointment     the related appointment (non-null)
     * @param overrideTitle   optional custom title (null to use default)
     * @param overrideMessage optional custom message (null to use default)
     */
    void sendAppointmentNotification(
            Long targetUserId,
            NotificationType type,
            Appointment appointment,
            String overrideTitle,
            String overrideMessage
    );
}
