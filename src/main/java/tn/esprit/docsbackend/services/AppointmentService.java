package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.appointment.AppointmentCreateRequest;
import tn.esprit.docsbackend.dto.appointment.AppointmentDto;
import tn.esprit.docsbackend.dto.appointment.AppointmentStatusUpdateRequest;
import tn.esprit.docsbackend.dto.appointment.DoctorScheduleDto;
import tn.esprit.docsbackend.dto.appointment.WeeklyCalendarResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {

    // -------- Doctor schedule ----------
    List<DoctorScheduleDto> getScheduleForCurrentDoctor();

    /**
     * Replace current doctor's weekly schedule with the provided entries.
     */
    List<DoctorScheduleDto> updateScheduleForCurrentDoctor(List<DoctorScheduleDto> entries);

    // -------- Appointments -------------
    AppointmentDto createAppointmentAsCurrentPatient(AppointmentCreateRequest request);

    List<AppointmentDto> getAppointmentsForCurrentPatient(LocalDateTime from, LocalDateTime to);

    List<AppointmentDto> getAppointmentsForCurrentDoctor(LocalDateTime from, LocalDateTime to);

    /**
     * Cancel an appointment if current user is either the doctor or the patient.
     */
    void cancelAppointment(Long appointmentId);

    // -------- Existing doctor schedule / slots APIs --------
    List<DoctorScheduleDto> getScheduleForDoctor(Long doctorId);

    List<String> getDoctorAvailableSlots(Long doctorId, LocalDateTime from, LocalDateTime to);

    AppointmentDto updateAppointmentStatus(Long id, AppointmentStatusUpdateRequest request);

    AppointmentDto rescheduleAppointment(Long id, AppointmentCreateRequest request);

    // -------- Weekly calendar APIs --------

    /**
     * Weekly calendar for a specific doctor (doctorId is DoctorProfile.id).
     */
    WeeklyCalendarResponse getWeeklyCalendarForDoctor(Long doctorId, LocalDate weekStart);

    /**
     * Weekly calendar for the currently authenticated doctor.
     */
    WeeklyCalendarResponse getWeeklyCalendarForCurrentDoctor(LocalDate weekStart);
}
