package tn.esprit.docsbackend;

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
import tn.esprit.docsbackend.controllers.AppointmentController;
import tn.esprit.docsbackend.dto.appointment.AppointmentCreateRequest;
import tn.esprit.docsbackend.dto.appointment.AppointmentDto;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;
import tn.esprit.docsbackend.services.AppointmentService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AppointmentController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @Test
    @DisplayName("POST /api/appointments - doit créer un rendez-vous")
    void requestAppointment_shouldReturnOk() throws Exception {

        AppointmentCreateRequest request = new AppointmentCreateRequest();
        request.setDoctorUserId(2L);
        request.setDate(LocalDate.of(2025, 1, 10));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(10, 30));
        request.setReason("Consultation de suivi");

        AppointmentDto dto = AppointmentDto.builder()
                .id(1L)
                .doctorUserId(2L)
                .patientUserId(10L)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .status(AppointmentStatus.PENDING)
                .build();

        Mockito.when(appointmentService.requestAppointmentAsPatient(any()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.doctorUserId").value(2L))
                .andExpect(jsonPath("$.patientUserId").value(10L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
