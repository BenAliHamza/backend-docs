// File: src/main/java/tn/esprit/docsbackend/services/impl/AppointmentServiceImpl.java
package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.appointment.AppointmentBookingRequest;
import tn.esprit.docsbackend.dto.appointment.AvailabilitySessionRequest;
import tn.esprit.docsbackend.dto.appointment.AvailabilitySessionResponse;
import tn.esprit.docsbackend.dto.appointment.SlotDto;
import tn.esprit.docsbackend.entities.AppointmentSlot;
import tn.esprit.docsbackend.entities.AvailabilitySession;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.RecurrenceType;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.entities.enums.SlotStatus;
import tn.esprit.docsbackend.repositories.AppointmentSlotRepository;
import tn.esprit.docsbackend.repositories.AvailabilitySessionRepository;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.services.AppointmentService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final AvailabilitySessionRepository availabilitySessionRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;

    // ─────────────────────────────────────────────────────────────────────────────
    // 1) Doctor creates availability → generate slots
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public AvailabilitySessionResponse createAvailabilityForCurrentDoctor(AvailabilitySessionRequest request) {
        // Ensure current user is a DOCTOR
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor profile not found")
                );

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : startDate;
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        if (startDate == null || startTime == null || endTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing start/end date or time");
        }

        if (!endDate.isAfter(startDate) && !endDate.isEqual(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be >= startDate");
        }

        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }

        if (request.getSlotDurationMinutes() == null || request.getSlotDurationMinutes() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "slotDurationMinutes must be > 0");
        }

        RecurrenceType recurrenceType =
                request.getRecurrenceType() != null ? request.getRecurrenceType() : RecurrenceType.ONE_TIME;

        // Clean existing non-booked slots in this date range before regenerating
        clearNonBookedSlotsForDoctorAndRange(doctorProfile, startDate, endDate);

        AvailabilitySession session = AvailabilitySession.builder()
                .doctorProfile(doctorProfile)
                .startDate(startDate)
                .endDate(endDate)
                .startTime(startTime)
                .endTime(endTime)
                .slotDurationMinutes(request.getSlotDurationMinutes())
                .recurrenceType(recurrenceType)
                .daysOfWeek(request.getDaysOfWeek())
                .build();

        availabilitySessionRepository.save(session);

        int generated = generateSlotsForSession(session);

        return AvailabilitySessionResponse.builder()
                .id(session.getId())
                .generatedSlotsCount(generated)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2) Doctor fetches his slots in a range
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public List<SlotDto> getSlotsForCurrentDoctor(LocalDate from, LocalDate to, SlotStatus status) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to are required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor profile not found")
                );

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.plusDays(1).atStartOfDay(); // inclusive end date

        List<AppointmentSlot> slots;

        if (status != null) {
            slots = appointmentSlotRepository
                    .findByDoctorProfileAndStartDateTimeBetweenAndStatus(
                            doctorProfile, fromDt, toDt, status
                    );
        } else {
            slots = appointmentSlotRepository
                    .findByDoctorProfileAndStartDateTimeBetween(
                            doctorProfile, fromDt, toDt
                    );
        }

        return toSlotDtos(slots);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3) Patient fetches available slots for a doctor
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public List<SlotDto> getAvailableSlotsForDoctorProfile(Long doctorProfileId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to are required");
        }

        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found")
                );

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.plusDays(1).atStartOfDay();

        List<AppointmentSlot> slots = appointmentSlotRepository
                .findByDoctorProfileAndStartDateTimeBetweenAndStatus(
                        doctorProfile, fromDt, toDt, SlotStatus.AVAILABLE
                );

        return toSlotDtos(slots);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4) Patient books a slot
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public SlotDto bookSlotForCurrentPatient(AppointmentBookingRequest request) {
        if (request.getSlotId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "slotId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient profile not found")
                );

        AppointmentSlot slot = appointmentSlotRepository
                .findByIdAndStatus(request.getSlotId(), SlotStatus.AVAILABLE)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot not available")
                );

        slot.setPatientProfile(patientProfile);
        slot.setStatus(SlotStatus.BOOKED);
        appointmentSlotRepository.save(slot);

        // In the future you can also create an Appointment entity here if needed.

        return toSlotDto(slot);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Helper: generate slots for a session
    // ─────────────────────────────────────────────────────────────────────────────
    private int generateSlotsForSession(AvailabilitySession session) {
        List<AppointmentSlot> toSave = new ArrayList<>();

        LocalDate startDate = session.getStartDate();
        LocalDate endDate = session.getEndDate();
        RecurrenceType recurrenceType = session.getRecurrenceType();
        List<DayOfWeek> days = session.getDaysOfWeek();

        if (recurrenceType == RecurrenceType.ONE_TIME) {
            // Every day between startDate and endDate
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                generateSlotsForSingleDay(session, current, toSave);
                current = current.plusDays(1);
            }
        } else if (recurrenceType == RecurrenceType.WEEKLY) {
            if (days == null || days.isEmpty()) {
                return 0;
            }
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                if (days.contains(current.getDayOfWeek())) {
                    generateSlotsForSingleDay(session, current, toSave);
                }
                current = current.plusDays(1);
            }
        }

        appointmentSlotRepository.saveAll(toSave);
        return toSave.size();
    }

    private void generateSlotsForSingleDay(AvailabilitySession session, LocalDate day, List<AppointmentSlot> collector) {
        LocalTime startTime = session.getStartTime();
        LocalTime endTime = session.getEndTime();
        int duration = session.getSlotDurationMinutes();

        LocalDateTime cursor = LocalDateTime.of(day, startTime);
        LocalDateTime end = LocalDateTime.of(day, endTime);

        while (cursor.plusMinutes(duration).compareTo(end) <= 0) {
            LocalDateTime slotStart = cursor;
            LocalDateTime slotEnd = cursor.plusMinutes(duration);

            AppointmentSlot slot = AppointmentSlot.builder()
                    .doctorProfile(session.getDoctorProfile())
                    .availabilitySession(session)
                    .startDateTime(slotStart)
                    .endDateTime(slotEnd)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            collector.add(slot);

            cursor = slotEnd;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Helper: remove old non-booked slots when doctor resets availability
    // ─────────────────────────────────────────────────────────────────────────────
    private void clearNonBookedSlotsForDoctorAndRange(DoctorProfile doctorProfile,
                                                      LocalDate from,
                                                      LocalDate to) {
        // We don't want to delete BOOKED slots, only others
        List<AppointmentSlot> allNonBooked = appointmentSlotRepository
                .findByDoctorProfileAndStatusNot(doctorProfile, SlotStatus.BOOKED);

        if (allNonBooked.isEmpty()) {
            return;
        }

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.plusDays(1).atStartOfDay();

        List<AppointmentSlot> toDelete = new ArrayList<>();

        for (AppointmentSlot slot : allNonBooked) {
            LocalDateTime start = slot.getStartDateTime();
            if (start != null &&
                    !start.isBefore(fromDt) &&
                    start.isBefore(toDt)) {
                toDelete.add(slot);
            }
        }

        if (!toDelete.isEmpty()) {
            appointmentSlotRepository.deleteAll(toDelete);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DTO mapping helpers
    // ─────────────────────────────────────────────────────────────────────────────
    private List<SlotDto> toSlotDtos(List<AppointmentSlot> slots) {
        List<SlotDto> result = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            result.add(toSlotDto(slot));
        }
        return result;
    }

    private SlotDto toSlotDto(AppointmentSlot slot) {
        Long patientProfileId = slot.getPatientProfile() != null
                ? slot.getPatientProfile().getId()
                : null;

        return SlotDto.builder()
                .id(slot.getId())
                .doctorProfileId(slot.getDoctorProfile().getId())
                .patientProfileId(patientProfileId)
                .startDateTime(slot.getStartDateTime())
                .endDateTime(slot.getEndDateTime())
                .status(slot.getStatus())
                .build();
    }
}
