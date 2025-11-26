package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.appointment.*;
import tn.esprit.docsbackend.dto.common.ListResponse;
import tn.esprit.docsbackend.services.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentDto> createAppointment(@RequestBody AppointmentCreateRequest request) {
        AppointmentDto dto = appointmentService.createAppointmentAsCurrentPatient(request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/appointments/me")
    public ResponseEntity<ListResponse<AppointmentDto>> getMyAppointments(
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<AppointmentDto> list = appointmentService.getAppointmentsForCurrentPatient(from, to);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    @GetMapping("/doctors/me/appointments")
    public ResponseEntity<ListResponse<AppointmentDto>> getDoctorAppointments(
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<AppointmentDto> list = appointmentService.getAppointmentsForCurrentDoctor(from, to);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable("id") Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // -------- NEW ENDPOINT: doctor updates appointment status --------
    @PatchMapping("/appointments/{id}/status")
    public ResponseEntity<AppointmentDto> updateAppointmentStatus(
            @PathVariable("id") Long id,
            @RequestBody AppointmentStatusUpdateRequest request
    ) {
        AppointmentDto dto = appointmentService.updateAppointmentStatus(id, request);
        return ResponseEntity.ok(dto);
    }

    // -------- NEW ENDPOINT: reschedule appointment --------
    @PutMapping("/appointments/{id}")
    public ResponseEntity<AppointmentDto> rescheduleAppointment(
            @PathVariable("id") Long id,
            @RequestBody AppointmentCreateRequest request
    ) {
        AppointmentDto dto = appointmentService.rescheduleAppointment(id, request);
        return ResponseEntity.ok(dto);
    }
}
