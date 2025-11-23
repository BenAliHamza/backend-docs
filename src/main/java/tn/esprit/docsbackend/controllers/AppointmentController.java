package tn.esprit.docsbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.appointment.AppointmentCreateRequest;
import tn.esprit.docsbackend.dto.appointment.AppointmentDto;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;
import tn.esprit.docsbackend.services.AppointmentService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor

public class AppointmentController {

    private final AppointmentService appointmentService;

    // PATIENT : créer une demande
    @PostMapping
    public AppointmentDto createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        return appointmentService.requestAppointmentAsPatient(request);
    }

    // PATIENT : historique de ses RDV
    @GetMapping("/me/patient")
    public List<AppointmentDto> getMyAppointmentsAsPatient() {
        return appointmentService.getMyAppointmentsAsPatient();
    }

    // DOCTOR : liste de ses RDV (avec filtres simples)
    @GetMapping("/me/doctor")
    public List<AppointmentDto> getMyAppointmentsAsDoctor(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return appointmentService.getMyAppointmentsAsDoctor(status, fromDate, toDate);
    }

    // DOCTOR : accepter / refuser / compléter
    @PutMapping("/{id}/accept")
    public AppointmentDto accept(@PathVariable Long id) {
        return appointmentService.acceptAppointment(id);
    }

    @PutMapping("/{id}/reject")
    public AppointmentDto reject(@PathVariable Long id) {
        return appointmentService.rejectAppointment(id);
    }

    @PutMapping("/{id}/complete")
    public AppointmentDto complete(@PathVariable Long id) {
        return appointmentService.completeAppointment(id);
    }
}
