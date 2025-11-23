// src/main/java/tn/esprit/docsbackend/controllers/AppointmentController.java
package tn.esprit.docsbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.appointment.AppointmentBookingRequest;
import tn.esprit.docsbackend.dto.appointment.AvailabilitySessionRequest;
import tn.esprit.docsbackend.dto.appointment.AvailabilitySessionResponse;
import tn.esprit.docsbackend.dto.appointment.SlotDto;
import tn.esprit.docsbackend.entities.enums.SlotStatus;
import tn.esprit.docsbackend.services.AppointmentService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/doctors/me/availability")
    public AvailabilitySessionResponse createAvailabilityForCurrentDoctor(
            @Valid @RequestBody AvailabilitySessionRequest request
    ) {
        return appointmentService.createAvailabilityForCurrentDoctor(request);
    }

    @GetMapping("/doctors/me/slots")
    public List<SlotDto> getSlotsForCurrentDoctor(
            @RequestParam("from") LocalDate from,
            @RequestParam("to") LocalDate to,
            @RequestParam(value = "status", required = false) SlotStatus status
    ) {
        return appointmentService.getSlotsForCurrentDoctor(from, to, status);
    }

    @GetMapping("/doctors/{doctorProfileId}/slots")
    public List<SlotDto> getAvailableSlotsForDoctor(
            @PathVariable Long doctorProfileId,
            @RequestParam("from") LocalDate from,
            @RequestParam("to") LocalDate to
    ) {
        return appointmentService.getAvailableSlotsForDoctorProfile(doctorProfileId, from, to);
    }

    @PostMapping("/patients/me/appointments")
    public SlotDto bookSlotForCurrentPatient(
            @Valid @RequestBody AppointmentBookingRequest request
    ) {
        return appointmentService.bookSlotForCurrentPatient(request);
    }
}
