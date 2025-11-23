// src/main/java/tn/esprit/docsbackend/services/AppointmentService.java
package tn.esprit.docsbackend.services;

import tn.esprit.docsbackend.dto.appointment.AppointmentBookingRequest;
import tn.esprit.docsbackend.dto.appointment.AvailabilitySessionRequest;
import tn.esprit.docsbackend.dto.appointment.AvailabilitySessionResponse;
import tn.esprit.docsbackend.dto.appointment.SlotDto;
import tn.esprit.docsbackend.entities.enums.SlotStatus;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AvailabilitySessionResponse createAvailabilityForCurrentDoctor(AvailabilitySessionRequest request);

    List<SlotDto> getSlotsForCurrentDoctor(LocalDate from, LocalDate to, SlotStatus status);

    List<SlotDto> getAvailableSlotsForDoctorProfile(Long doctorProfileId, LocalDate from, LocalDate to);

    SlotDto bookSlotForCurrentPatient(AppointmentBookingRequest request);
}
