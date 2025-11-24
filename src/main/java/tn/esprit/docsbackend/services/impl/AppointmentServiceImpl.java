// src/main/java/tn/esprit/docsbackend/services/impl/AppointmentServiceImpl.java
package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.appointment.AppointmentBookingRequest;
import tn.esprit.docsbackend.dto.appointment.AvailabilitySessionRequest;
import tn.esprit.docsbackend.dto.appointment.AvailabilitySessionResponse;
import tn.esprit.docsbackend.dto.appointment.SlotDto;
import tn.esprit.docsbackend.entities.*;
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

    @Override
    public AvailabilitySessionResponse createAvailabilityForCurrentDoctor(AvailabilitySessionRequest request) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);
        DoctorProfile doctorProfile = doctorProfileRepository.findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor profile not found"));

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : startDate;
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        if (startDate == null || startTime == null || endTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing start/end date or time");
        }

        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }

        clearNonBookedSlotsForDoctorAndRange(doctorProfile, startDate, endDate);

        AvailabilitySession session = AvailabilitySession.builder()
                .doctorProfile(doctorProfile)
                .startDate(startDate)
                .endDate(endDate)
                .startTime(startTime)
                .endTime(endTime)
                .slotDurationMinutes(request.getSlotDurationMinutes())
                .recurrenceType(request.getRecurrenceType())
                .daysOfWeek(request.getDaysOfWeek())
                .build();

        availabilitySessionRepository.save(session);

        int generated = generateSlotsForSession(session);

        return AvailabilitySessionResponse.builder()
                .id(session.getId())
                .generatedSlotsCount(generated)
                .build();
    }

    @Override
    public List<SlotDto> getSlotsForCurrentDoctor(LocalDate from, LocalDate to, SlotStatus status) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);
        DoctorProfile doctorProfile = doctorProfileRepository.findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor profile not found"));

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.plusDays(1).atStartOfDay();

        List<AppointmentSlot> slots = status != null
                ? appointmentSlotRepository.findByDoctorProfileAndStartDateTimeBetweenAndStatus(doctorProfile, fromDt, toDt, status)
                : appointmentSlotRepository.findByDoctorProfileAndStartDateTimeBetween(doctorProfile, fromDt, toDt);

        return toSlotDtos(slots);
    }

    @Override
    public List<SlotDto> getAvailableSlotsForDoctorProfile(Long doctorProfileId, LocalDate from, LocalDate to) {
        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.plusDays(1).atStartOfDay();

        List<AppointmentSlot> slots = appointmentSlotRepository
                .findByDoctorProfileAndStartDateTimeBetweenAndStatus(doctorProfile, fromDt, toDt, SlotStatus.AVAILABLE);

        return toSlotDtos(slots);
    }

    @Override
    public SlotDto bookSlotForCurrentPatient(AppointmentBookingRequest request) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);
        PatientProfile patientProfile = patientProfileRepository.findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient profile not found"));

        AppointmentSlot slot = appointmentSlotRepository
                .findByIdAndStatus(request.getSlotId(), SlotStatus.AVAILABLE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot not available"));

        if (slot.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot book past slot");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slot.setPatientProfile(patientProfile);
        appointmentSlotRepository.save(slot);

        return toSlotDto(slot);
    }

    private void clearNonBookedSlotsForDoctorAndRange(DoctorProfile doctorProfile,
                                                      LocalDate startDate,
                                                      LocalDate endDate) {

        LocalDateTime fromDt = startDate.atStartOfDay();
        LocalDateTime toDt = endDate.plusDays(1).atStartOfDay();

        List<AppointmentSlot> existing = appointmentSlotRepository
                .findByDoctorProfileAndStartDateTimeBetween(doctorProfile, fromDt, toDt);

        if (existing == null || existing.isEmpty()) {
            return;
        }

        List<AppointmentSlot> toDelete = existing.stream()
                .filter(slot -> slot.getStatus() != SlotStatus.BOOKED)
                .toList();

        if (!toDelete.isEmpty()) {
            appointmentSlotRepository.deleteAll(toDelete);
        }
    }

    private int generateSlotsForSession(AvailabilitySession session) {
        LocalDate startDate = session.getStartDate();
        LocalDate endDate = session.getEndDate() != null ? session.getEndDate() : startDate;
        int durationMinutes = session.getSlotDurationMinutes();

        if (durationMinutes <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "slotDurationMinutes must be > 0");
        }

        List<DayOfWeek> selectedDays = session.getDaysOfWeek();
        boolean hasDaysFilter = selectedDays != null && !selectedDays.isEmpty();

        List<AppointmentSlot> toSave = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {

            boolean shouldGenerate = !hasDaysFilter || selectedDays.contains(current.getDayOfWeek());

            if (shouldGenerate) {
                LocalTime t = session.getStartTime();
                while (t.isBefore(session.getEndTime())) {
                    LocalDateTime slotStart = LocalDateTime.of(current, t);
                    LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

                    if (slotEnd.isAfter(LocalDateTime.of(current, session.getEndTime()))) {
                        break;
                    }

                    if (slotEnd.isBefore(LocalDateTime.now())) {
                        t = t.plusMinutes(durationMinutes);
                        continue;
                    }

                    AppointmentSlot slot = AppointmentSlot.builder()
                            .doctorProfile(session.getDoctorProfile())
                            .availabilitySession(session)
                            .startDateTime(slotStart)
                            .endDateTime(slotEnd)
                            .status(SlotStatus.AVAILABLE)
                            .build();

                    toSave.add(slot);
                    t = t.plusMinutes(durationMinutes);
                }
            }

            current = current.plusDays(1);
        }

        appointmentSlotRepository.saveAll(toSave);
        return toSave.size();
    }

    private List<SlotDto> toSlotDtos(List<AppointmentSlot> slots) {
        List<SlotDto> result = new ArrayList<>();
        if (slots != null) {
            for (AppointmentSlot slot : slots) {
                result.add(toSlotDto(slot));
            }
        }
        return result;
    }

    private SlotDto toSlotDto(AppointmentSlot slot) {
        Long patientProfileId = slot.getPatientProfile() != null ? slot.getPatientProfile().getId() : null;
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
