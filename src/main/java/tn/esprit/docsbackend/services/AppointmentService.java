package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.appointment.*;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {

    // ---- Doctor schedule management ----
    List<DoctorScheduleDto> getScheduleForCurrentDoctor();
    List<DoctorScheduleDto> updateScheduleForCurrentDoctor(List<DoctorScheduleDto> entries);

    // ---- Appointments ----
    AppointmentDto createAppointmentAsCurrentPatient(AppointmentCreateRequest request);
    List<AppointmentDto> getAppointmentsForCurrentPatient(LocalDateTime from, LocalDateTime to);
    List<AppointmentDto> getAppointmentsForCurrentDoctor(LocalDateTime from, LocalDateTime to);
    void cancelAppointment(Long appointmentId);

    // ---- NEW ENDPOINTS ----
    List<DoctorScheduleDto> getScheduleForDoctor(Long doctorId);

    List<String> getDoctorAvailableSlots(Long doctorId, LocalDateTime from, LocalDateTime to);

    AppointmentDto updateAppointmentStatus(Long id, AppointmentStatusUpdateRequest request);

    AppointmentDto rescheduleAppointment(Long id, AppointmentCreateRequest request);
}
