package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.appointment.DoctorScheduleDto;
import tn.esprit.docsbackend.dto.common.ListResponse;
import tn.esprit.docsbackend.services.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final AppointmentService appointmentService;

    @GetMapping("/doctors/me/schedule")
    public ResponseEntity<ListResponse<DoctorScheduleDto>> getMySchedule() {
        List<DoctorScheduleDto> list = appointmentService.getScheduleForCurrentDoctor();
        return ResponseEntity.ok(ListResponse.of(list));
    }

    @PutMapping("/doctors/me/schedule")
    public ResponseEntity<ListResponse<DoctorScheduleDto>> updateMySchedule(
            @RequestBody List<DoctorScheduleDto> entries
    ) {
        List<DoctorScheduleDto> list = appointmentService.updateScheduleForCurrentDoctor(entries);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    // -------- NEW ENDPOINT: public doctor schedule --------
    @GetMapping("/doctors/{doctorId}/schedule")
    public ResponseEntity<ListResponse<DoctorScheduleDto>> getDoctorSchedule(@PathVariable Long doctorId) {
        List<DoctorScheduleDto> list = appointmentService.getScheduleForDoctor(doctorId);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    // -------- NEW ENDPOINT: public available slots --------
    @GetMapping("/doctors/{doctorId}/available-slots")
    public ResponseEntity<ListResponse<String>> getDoctorAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<String> slots = appointmentService.getDoctorAvailableSlots(doctorId, from, to);
        return ResponseEntity.ok(ListResponse.of(slots));
    }
}
