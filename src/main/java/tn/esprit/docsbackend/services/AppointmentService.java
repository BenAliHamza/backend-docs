package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.appointment.AppointmentCreateRequest;
import tn.esprit.docsbackend.dto.appointment.AppointmentDto;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    // PATIENT : créer une demande de RDV
    AppointmentDto requestAppointmentAsPatient(AppointmentCreateRequest request);

    // PATIENT : historique de ses RDV
    List<AppointmentDto> getMyAppointmentsAsPatient();

    // DOCTOR : liste de ses RDV (optionnellement filtrés)
    List<AppointmentDto> getMyAppointmentsAsDoctor(AppointmentStatus status,
                                                   LocalDate from,
                                                   LocalDate to);

    // DOCTOR : accepter / refuser / terminer
    AppointmentDto acceptAppointment(Long appointmentId);

    AppointmentDto rejectAppointment(Long appointmentId);

    AppointmentDto completeAppointment(Long appointmentId);
}
