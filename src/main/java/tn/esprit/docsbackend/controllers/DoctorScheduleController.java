package tn.esprit.docsbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.docsbackend.dto.appointment.DoctorScheduleDto;
import tn.esprit.docsbackend.dto.appointment.WeeklyCalendarResponse;
import tn.esprit.docsbackend.dto.common.ListResponse;
import tn.esprit.docsbackend.services.AppointmentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final AppointmentService appointmentService;

    /**
     * GET /api/doctors/me/schedule
     * Current doctor -> weekly schedule (raw schedule entries).
     */
    @GetMapping("/doctors/me/schedule")
    public ResponseEntity<ListResponse<DoctorScheduleDto>> getMySchedule() {
        List<DoctorScheduleDto> list = appointmentService.getScheduleForCurrentDoctor();
        return ResponseEntity.ok(ListResponse.of(list));
    }

    /**
     * PUT /api/doctors/me/schedule
     * Replace weekly schedule for current doctor.
     */
    @PutMapping("/doctors/me/schedule")
    public ResponseEntity<ListResponse<DoctorScheduleDto>> updateMySchedule(
            @RequestBody List<DoctorScheduleDto> entries
    ) {
        List<DoctorScheduleDto> list = appointmentService.updateScheduleForCurrentDoctor(entries);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    /**
     * GET /api/doctors/{doctorId}/schedule
     * Public: patient viewing a specific doctor's raw weekly schedule.
     */
    @GetMapping("/doctors/{doctorId}/schedule")
    public ResponseEntity<ListResponse<DoctorScheduleDto>> getDoctorSchedule(@PathVariable Long doctorId) {
        List<DoctorScheduleDto> list = appointmentService.getScheduleForDoctor(doctorId);
        return ResponseEntity.ok(ListResponse.of(list));
    }

    /**
     * GET /api/doctors/{doctorId}/available-slots
     * Public: patient viewing a specific doctor's available appointment slots
     * in a given date-time range (computed from schedule + existing appointments).
     *
     * Returns a list of "start/end" ISO-8601 strings.
     */
    @GetMapping("/doctors/{doctorId}/available-slots")
    public ResponseEntity<ListResponse<String>> getDoctorAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<String> slots = appointmentService.getDoctorAvailableSlots(doctorId, from, to);
        return ResponseEntity.ok(ListResponse.of(slots));
    }

    /**
     * GET /api/doctors/{doctorId}/weekly-calendar
     * Public weekly calendar for a doctor (patient-facing).
     *
     * weekStart: optional, if null -> current week (Monday as start).
     */
    @GetMapping("/doctors/{doctorId}/weekly-calendar")
    public ResponseEntity<WeeklyCalendarResponse> getDoctorWeeklyCalendar(
            @PathVariable Long doctorId,
            @RequestParam(name = "weekStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        WeeklyCalendarResponse response = appointmentService.getWeeklyCalendarForDoctor(doctorId, weekStart);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/doctors/me/weekly-calendar
     * Weekly calendar for the currently authenticated doctor (shortcut).
     */
    @GetMapping("/doctors/me/weekly-calendar")
    public ResponseEntity<WeeklyCalendarResponse> getMyWeeklyCalendar(
            @RequestParam(name = "weekStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        WeeklyCalendarResponse response = appointmentService.getWeeklyCalendarForCurrentDoctor(weekStart);
        return ResponseEntity.ok(response);
    }
}
