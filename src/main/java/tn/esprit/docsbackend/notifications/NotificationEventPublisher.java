package tn.esprit.docsbackend.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.enums.NotificationType;
import tn.esprit.docsbackend.services.NotificationService;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final NotificationService notificationService;
    private final AppointmentEventMapper eventMapper;

    public void publishToDoctor(AppointmentEventType event, Appointment appt) {
        if (appt == null || appt.getDoctor() == null || appt.getDoctor().getUser() == null) {
            return;
        }

        Long doctorUserId = appt.getDoctor().getUser().getId();

        notificationService.sendAppointmentNotification(
                doctorUserId,
                mapEvent(event),
                appt,
                eventMapper.buildTitle(event),
                eventMapper.buildMessage(event, appt)
        );
    }

    public void publishToPatient(AppointmentEventType event, Appointment appt) {
        if (appt == null || appt.getPatient() == null || appt.getPatient().getUser() == null) {
            return;
        }

        Long patientUserId = appt.getPatient().getUser().getId();

        notificationService.sendAppointmentNotification(
                patientUserId,
                mapEvent(event),
                appt,
                eventMapper.buildTitle(event),
                eventMapper.buildMessage(event, appt)
        );
    }

    private NotificationType mapEvent(AppointmentEventType event) {
        return switch (event) {
            case CREATED -> NotificationType.APPOINTMENT_REQUESTED;
            case ACCEPTED -> NotificationType.APPOINTMENT_ACCEPTED;
            case REJECTED -> NotificationType.APPOINTMENT_REJECTED;
            case CANCELLED -> NotificationType.APPOINTMENT_CANCELLED;
            case RESCHEDULED -> NotificationType.APPOINTMENT_RESCHEDULED;
        };
    }
}
