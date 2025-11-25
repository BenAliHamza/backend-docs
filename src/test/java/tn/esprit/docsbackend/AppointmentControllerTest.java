package tn.esprit.docsbackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.docsbackend.dto.appointment.AppointmentCreateRequest;
import tn.esprit.docsbackend.dto.appointment.AppointmentDto;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;
import tn.esprit.docsbackend.services.AppointmentService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @Test
    @DisplayName("POST /api/appointments - doit créer un rendez-vous pour un patient")
    void requestAppointment_shouldReturnOk() throws Exception {

        // GIVEN – Request
        AppointmentCreateRequest request = new AppointmentCreateRequest();
        request.setDoctorUserId(2L);
        request.setDate(LocalDate.of(2025, 1, 10));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(10, 30));
        request.setReason("Consultation de suivi");

        // GIVEN – Response from service
        AppointmentDto dto = new AppointmentDto();
        dto.setId(1L);
        dto.setDoctorUserId(2L);
        dto.setPatientUserId(10L);
        dto.setDate(request.getDate());
        dto.setStartTime(request.getStartTime());
        dto.setEndTime(request.getEndTime());
        dto.setReason(request.getReason());
        dto.setStatus(AppointmentStatus.PENDING);

        Mockito.when(appointmentService.requestAppointmentAsPatient(any(AppointmentCreateRequest.class)))
                .thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())  // ton controller renvoie 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.doctorUserId").value(2L))
                .andExpect(jsonPath("$.patientUserId").value(10L))
                .andExpect(jsonPath("$.reason").value("Consultation de suivi"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
