package tn.esprit.docsbackend.notifications;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.User;

@Component
public class AppointmentEventMapper {

    public String buildTitle(AppointmentEventType event) {
        return switch (event) {
            case CREATED -> "New appointment request";
            case ACCEPTED -> "Appointment accepted";
            case REJECTED -> "Appointment rejected";
            case CANCELLED -> "Appointment cancelled";
            case RESCHEDULED -> "Appointment rescheduled";
        };
    }

    public String buildMessage(AppointmentEventType event, Appointment appt) {

        User doctor = appt.getDoctor().getUser();
        User patient = appt.getPatient().getUser();

        String doctorName = doctor.getFirstname() + " " + doctor.getLastname();
        String patientName = patient.getFirstname() + " " + patient.getLastname();
        String startAt = appt.getStartAt().toString();

        return switch (event) {
            case CREATED -> patientName + " requested an appointment for " + startAt + ".";
            case ACCEPTED -> "Dr. " + doctorName + " accepted your appointment for " + startAt + ".";
            case REJECTED -> "Dr. " + doctorName + " rejected your appointment.";
            case CANCELLED -> "The appointment scheduled for " + startAt + " was cancelled.";
            case RESCHEDULED ->
                    "Your appointment was rescheduled. New start time: " + startAt + ".";
        };
    }
}
