package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.appointment.AppointmentCreateRequest;
import tn.esprit.docsbackend.dto.appointment.AppointmentDto;
import tn.esprit.docsbackend.dto.appointment.AppointmentStatusUpdateRequest;
import tn.esprit.docsbackend.dto.appointment.DoctorScheduleDto;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.DoctorSchedule;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.entities.enums.UserStatus;
import tn.esprit.docsbackend.repositories.AppointmentRepository;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.DoctorScheduleRepository;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.repositories.UserRepository;
import tn.esprit.docsbackend.services.AppointmentService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    // ==================== Doctor schedule ====================

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleDto> getScheduleForCurrentDoctor() {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        return doctorScheduleRepository
                .findByDoctorIdAndDeletedFalseAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorProfile.getId())
                .stream()
                .map(this::toScheduleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<DoctorScheduleDto> updateScheduleForCurrentDoctor(List<DoctorScheduleDto> entries) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        // Mark existing schedules as deleted (soft delete) to keep things simple.
        List<DoctorSchedule> existing = doctorScheduleRepository
                .findByDoctorIdAndDeletedFalseAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorProfile.getId());
        for (DoctorSchedule s : existing) {
            s.setDeleted(true);
        }
        doctorScheduleRepository.saveAll(existing);

        if (entries == null || entries.isEmpty()) {
            // Doctor chose to clear schedule
            return List.of();
        }

        List<DoctorSchedule> toSave = entries.stream()
                .map(dto -> fromScheduleDtoForDoctor(dto, doctorProfile))
                .collect(Collectors.toList());

        List<DoctorSchedule> saved = doctorScheduleRepository.saveAll(toSave);

        return saved.stream()
                .map(this::toScheduleDto)
                .collect(Collectors.toList());
    }

    /**
     * Public: get weekly schedule of a specific doctor by doctor profile id.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleDto> getScheduleForDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId is required");
        }

        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorId)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Doctor profile with id=" + doctorId + " not found"
                ));

        User doctorUser = doctorProfile.getUser();
        if (doctorUser == null
                || doctorUser.isDeleted()
                || doctorUser.getStatus() != UserStatus.ACTIVE
                || doctorUser.getRole() != Role.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not active");
        }

        return doctorScheduleRepository
                .findByDoctorIdAndDeletedFalseAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorProfile.getId())
                .stream()
                .map(this::toScheduleDto)
                .collect(Collectors.toList());
    }

    /**
     * Public: compute available appointment slots for a doctor between [from, to].
     * Returns a list of ISO-8601 time ranges formatted as "start/end".
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailableSlots(Long doctorId, LocalDateTime from, LocalDateTime to) {
        if (doctorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId is required");
        }
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to are required");
        }
        if (!to.isAfter(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "to must be after from");
        }

        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorId)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Doctor profile with id=" + doctorId + " not found"
                ));

        User doctorUser = doctorProfile.getUser();
        if (doctorUser == null
                || doctorUser.isDeleted()
                || doctorUser.getStatus() != UserStatus.ACTIVE
                || doctorUser.getRole() != Role.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not active");
        }

        List<DoctorSchedule> schedules = doctorScheduleRepository
                .findByDoctorIdAndDeletedFalseAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorProfile.getId());

        if (schedules.isEmpty()) {
            return List.of();
        }

        // Load all appointments for this doctor (we'll filter by date range in memory).
        List<Appointment> allAppointments = appointmentRepository
                .findByDoctorIdAndDeletedFalseOrderByStartAtAsc(doctorProfile.getId());

        LocalDate startDate = from.toLocalDate();
        LocalDate endDate = to.toLocalDate();

        List<String> result = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dow = date.getDayOfWeek();

            // Schedule entries for this day of week
            List<DoctorSchedule> daySchedules = schedules.stream()
                    .filter(s -> s.getDayOfWeek() == dow && Boolean.TRUE.equals(s.getActive()))
                    .toList();

            if (daySchedules.isEmpty()) {
                continue;
            }

            // Appointments on this date that should block availability
            LocalDate finalDate = date;
            List<Appointment> dayAppointments = allAppointments.stream()
                    .filter(a -> {
                        if (a == null || a.isDeleted() || a.getStartAt() == null || a.getEndAt() == null) {
                            return false;
                        }
                        if (!a.getStartAt().toLocalDate().equals(finalDate)) {
                            return false;
                        }
                        // Treat CANCELLED and REJECTED as non-blocking, others block.
                        if (a.getStatus() != null) {
                            String name = a.getStatus().name();
                            if ("CANCELLED".equals(name) || "REJECTED".equals(name)) {
                                return false;
                            }
                        }
                        // Only consider those overlapping the [from, to] window
                        LocalDateTime s = a.getStartAt();
                        LocalDateTime e = a.getEndAt();
                        return !e.isBefore(from) && s.isBefore(to);
                    })
                    .collect(Collectors.toList());

            for (DoctorSchedule schedule : daySchedules) {
                LocalDateTime slotStart = LocalDateTime.of(date, schedule.getStartTime());
                LocalDateTime slotEnd = LocalDateTime.of(date, schedule.getEndTime());

                // Clamp to global requested range
                if (slotEnd.isBefore(from) || !slotStart.isBefore(to)) {
                    continue;
                }
                if (slotStart.isBefore(from)) {
                    slotStart = from;
                }
                if (slotEnd.isAfter(to)) {
                    slotEnd = to;
                }

                // Start with the entire schedule slot, then subtract appointments.
                List<TimeRange> freeRanges = new ArrayList<>();
                freeRanges.add(new TimeRange(slotStart, slotEnd));

                for (Appointment appt : dayAppointments) {
                    LocalDateTime apptStart = appt.getStartAt();
                    LocalDateTime apptEnd = appt.getEndAt();

                    if (apptEnd.isBefore(slotStart) || !apptStart.isBefore(slotEnd)) {
                        continue; // no overlap with this schedule slot
                    }

                    List<TimeRange> updated = new ArrayList<>();
                    for (TimeRange range : freeRanges) {
                        // If no overlap between free range and appointment, keep range as is.
                        if (apptEnd.isBefore(range.start) || !apptStart.isBefore(range.end)) {
                            updated.add(range);
                            continue;
                        }

                        // Left part, before appointment
                        if (apptStart.isAfter(range.start)) {
                            updated.add(new TimeRange(range.start, apptStart));
                        }
                        // Right part, after appointment
                        if (apptEnd.isBefore(range.end)) {
                            updated.add(new TimeRange(apptEnd, range.end));
                        }
                    }
                    freeRanges = updated;
                }

                for (TimeRange range : freeRanges) {
                    if (range.start.isBefore(range.end)) {
                        // Represent as "start/end" in ISO-8601
                        result.add(range.start.toString() + "/" + range.end.toString());
                    }
                }
            }
        }

        return result;
    }

    // ==================== Appointments ====================

    @Override
    @Transactional
    public AppointmentDto createAppointmentAsCurrentPatient(AppointmentCreateRequest request) {
        if (request == null
                || request.getDoctorId() == null
                || request.getStartAt() == null
                || request.getEndAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId, startAt and endAt are required");
        }

        LocalDateTime startAt = request.getStartAt();
        LocalDateTime endAt = request.getEndAt();

        if (!startAt.toLocalDate().equals(endAt.toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment must start and end on the same day");
        }
        if (!endAt.isAfter(startAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt must be after startAt");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        DoctorProfile doctorProfile = doctorProfileRepository
                .findById(request.getDoctorId())
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Doctor profile with id=" + request.getDoctorId() + " not found"
                ));

        User doctorUser = doctorProfile.getUser();
        if (doctorUser == null
                || doctorUser.isDeleted()
                || doctorUser.getStatus() != UserStatus.ACTIVE
                || doctorUser.getRole() != Role.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not active");
        }

        // 1) Check within doctor's schedule
        if (!isWithinDoctorSchedule(doctorProfile.getId(), startAt, endAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not available at this time (outside schedule)");
        }

        // 2) Check doctor is not double-booked (SCHEDULED appointments)
        boolean hasOverlap = !appointmentRepository
                .findByDoctorIdAndDeletedFalseAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        doctorProfile.getId(),
                        AppointmentStatus.SCHEDULED,
                        endAt,
                        startAt
                )
                .isEmpty();

        if (hasOverlap) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor already has an appointment at this time");
        }

        Boolean tele = request.getTeleconsultation() != null && request.getTeleconsultation();

        Appointment entity = Appointment.builder()
                .doctor(doctorProfile)
                .patient(patientProfile)
                .startAt(startAt)
                .endAt(endAt)
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.getReason())
                .teleconsultation(tele)
                .build();

        Appointment saved = appointmentRepository.save(entity);
        return toAppointmentDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForCurrentPatient(LocalDateTime from, LocalDateTime to) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        List<Appointment> list;
        if (from == null && to == null) {
            list = appointmentRepository
                    .findByPatientIdAndDeletedFalseOrderByStartAtAsc(patientProfile.getId());
        } else {
            LocalDateTime fromEff = (from != null) ? from : LocalDateTime.MIN;
            LocalDateTime toEff = (to != null) ? to : LocalDateTime.MAX;
            list = appointmentRepository
                    .findByPatientIdAndDeletedFalseAndStartAtBetweenOrderByStartAtAsc(
                            patientProfile.getId(), fromEff, toEff);
        }

        return list.stream().map(this::toAppointmentDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForCurrentDoctor(LocalDateTime from, LocalDateTime to) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        List<Appointment> list;
        if (from == null && to == null) {
            list = appointmentRepository
                    .findByDoctorIdAndDeletedFalseOrderByStartAtAsc(doctorProfile.getId());
        } else {
            LocalDateTime fromEff = (from != null) ? from : LocalDateTime.MIN;
            LocalDateTime toEff = (to != null) ? to : LocalDateTime.MAX;
            list = appointmentRepository
                    .findByDoctorIdAndDeletedFalseAndStartAtBetweenOrderByStartAtAsc(
                            doctorProfile.getId(), fromEff, toEff);
        }

        return list.stream().map(this::toAppointmentDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId) {
        if (appointmentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appointmentId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        Appointment appt = appointmentRepository.findById(appointmentId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment with id=" + appointmentId + " not found"
                ));

        User doctorUser = appt.getDoctor().getUser();
        User patientUser = appt.getPatient().getUser();

        Long currentUserId = currentUser.getId();
        Long doctorUserId = (doctorUser != null) ? doctorUser.getId() : null;
        Long patientUserId = (patientUser != null) ? patientUser.getId() : null;

        if (!currentUserId.equals(doctorUserId) && !currentUserId.equals(patientUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to cancel this appointment");
        }

        if (appt.getStatus() != AppointmentStatus.SCHEDULED) {
            // For simplicity, we only allow cancel of SCHEDULED appointments
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only scheduled appointments can be cancelled");
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appt);
    }

    /**
     * Doctor updates appointment status (e.g. ACCEPTED, REJECTED, COMPLETED, etc.).
     */
    @Override
    @Transactional
    public AppointmentDto updateAppointmentStatus(Long appointmentId, AppointmentStatusUpdateRequest request) {
        if (appointmentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appointmentId is required");
        }
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        Appointment appt = appointmentRepository.findById(appointmentId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment with id=" + appointmentId + " not found"
                ));

        User doctorUser = appt.getDoctor() != null ? appt.getDoctor().getUser() : null;
        if (doctorUser == null || !currentUser.getId().equals(doctorUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this appointment");
        }

        if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelled appointments cannot be updated");
        }

        AppointmentStatus newStatus;
        try {
            newStatus = AppointmentStatus.valueOf(request.getStatus().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid appointment status: " + request.getStatus()
            );
        }

        appt.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appt);
        return toAppointmentDto(saved);
    }

    /**
     * Reschedule an existing appointment (same doctor & patient).
     * Only the doctor or the patient involved can reschedule.
     */
    @Override
    @Transactional
    public AppointmentDto rescheduleAppointment(Long appointmentId, AppointmentCreateRequest request) {
        if (appointmentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appointmentId is required");
        }
        if (request == null
                || request.getStartAt() == null
                || request.getEndAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startAt and endAt are required");
        }

        LocalDateTime startAt = request.getStartAt();
        LocalDateTime endAt = request.getEndAt();

        if (!startAt.toLocalDate().equals(endAt.toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment must start and end on the same day");
        }
        if (!endAt.isAfter(startAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt must be after startAt");
        }

        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        Appointment appt = appointmentRepository.findById(appointmentId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment with id=" + appointmentId + " not found"
                ));

        User doctorUser = appt.getDoctor() != null ? appt.getDoctor().getUser() : null;
        User patientUser = appt.getPatient() != null ? appt.getPatient().getUser() : null;

        Long currentUserId = currentUser.getId();
        Long doctorUserId = (doctorUser != null) ? doctorUser.getId() : null;
        Long patientUserId = (patientUser != null) ? patientUser.getId() : null;

        if (!currentUserId.equals(doctorUserId) && !currentUserId.equals(patientUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to reschedule this appointment");
        }

        if (appt.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only scheduled appointments can be rescheduled");
        }

        DoctorProfile doctorProfile = appt.getDoctor();
        if (doctorProfile == null || doctorProfile.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor profile is not available");
        }

        User apptDoctorUser = doctorProfile.getUser();
        if (apptDoctorUser == null
                || apptDoctorUser.isDeleted()
                || apptDoctorUser.getStatus() != UserStatus.ACTIVE
                || apptDoctorUser.getRole() != Role.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not active");
        }

        // Ensure doctorId is not changed during reschedule
        if (request.getDoctorId() != null && !request.getDoctorId().equals(doctorProfile.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change doctor when rescheduling");
        }

        // Check within doctor's schedule
        if (!isWithinDoctorSchedule(doctorProfile.getId(), startAt, endAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not available at this time (outside schedule)");
        }

        // Check overlapping with other SCHEDULED appointments (excluding this appointment)
        List<Appointment> overlaps = appointmentRepository
                .findByDoctorIdAndDeletedFalseAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        doctorProfile.getId(),
                        AppointmentStatus.SCHEDULED,
                        endAt,
                        startAt
                );

        boolean hasOtherOverlap = overlaps.stream()
                .anyMatch(other -> !other.getId().equals(appt.getId()));

        if (hasOtherOverlap) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor already has an appointment at this time");
        }

        appt.setStartAt(startAt);
        appt.setEndAt(endAt);

        if (request.getReason() != null) {
            appt.setReason(request.getReason());
        }
        if (request.getTeleconsultation() != null) {
            appt.setTeleconsultation(request.getTeleconsultation());
        }

        Appointment saved = appointmentRepository.save(appt);
        return toAppointmentDto(saved);
    }

    // ==================== Helper methods ====================

    private boolean isWithinDoctorSchedule(Long doctorId, LocalDateTime startAt, LocalDateTime endAt) {
        DayOfWeek dow = startAt.getDayOfWeek();
        LocalTime start = startAt.toLocalTime();
        LocalTime end = endAt.toLocalTime();

        List<DoctorSchedule> slots = doctorScheduleRepository
                .findByDoctorIdAndDeletedFalseAndActiveTrueAndDayOfWeekOrderByStartTimeAsc(doctorId, dow);

        if (slots.isEmpty()) {
            return false;
        }

        return slots.stream().anyMatch(s ->
                !start.isBefore(s.getStartTime()) && !end.isAfter(s.getEndTime())
        );
    }

    private DoctorScheduleDto toScheduleDto(DoctorSchedule s) {
        return DoctorScheduleDto.builder()
                .id(s.getId())
                .dayOfWeek(s.getDayOfWeek() != null ? s.getDayOfWeek().name() : null)
                .startTime(s.getStartTime() != null ? s.getStartTime().toString() : null)
                .endTime(s.getEndTime() != null ? s.getEndTime().toString() : null)
                .active(s.getActive())
                .build();
    }

    private DoctorSchedule fromScheduleDtoForDoctor(DoctorScheduleDto dto, DoctorProfile doctor) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule entry cannot be null");
        }
        if (dto.getDayOfWeek() == null || dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dayOfWeek, startTime and endTime are required");
        }

        DayOfWeek dow;
        try {
            dow = DayOfWeek.valueOf(dto.getDayOfWeek().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid dayOfWeek: " + dto.getDayOfWeek());
        }

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(dto.getStartTime());
            end = LocalTime.parse(dto.getEndTime());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time format (expected HH:mm)");
        }

        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }

        Boolean active = (dto.getActive() != null) ? dto.getActive() : Boolean.TRUE;

        return DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(dow)
                .startTime(start)
                .endTime(end)
                .active(active)
                .build();
    }

    private AppointmentDto toAppointmentDto(Appointment a) {
        DoctorProfile doctor = a.getDoctor();
        PatientProfile patient = a.getPatient();
        User doctorUser = doctor != null ? doctor.getUser() : null;
        User patientUser = patient != null ? patient.getUser() : null;

        return AppointmentDto.builder()
                .id(a.getId())
                .doctorId(doctor != null ? doctor.getId() : null)
                .doctorUserId(doctorUser != null ? doctorUser.getId() : null)
                .doctorFirstName(doctorUser != null ? doctorUser.getFirstname() : null)
                .doctorLastName(doctorUser != null ? doctorUser.getLastname() : null)
                .patientId(patient != null ? patient.getId() : null)
                .patientUserId(patientUser != null ? patientUser.getId() : null)
                .patientFirstName(patientUser != null ? patientUser.getFirstname() : null)
                .patientLastName(patientUser != null ? patientUser.getLastname() : null)
                .startAt(a.getStartAt())
                .endAt(a.getEndAt())
                .status(a.getStatus())
                .reason(a.getReason())
                .teleconsultation(a.getTeleconsultation())
                .build();
    }

    /**
     * Simple internal helper to represent a free time range.
     */
    private static class TimeRange {
        private final LocalDateTime start;
        private final LocalDateTime end;

        private TimeRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
