package tn.esprit.docsbackend.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.dto.appointment.AppointmentDto;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.User;

@Component
public class AppointmentMapper {

    public AppointmentDto toDto(Appointment entity) {
        if (entity == null) {
            return null;
        }

        User doctorUser = entity.getDoctor().getUser();
        User patientUser = entity.getPatient().getUser();

        return AppointmentDto.builder()
                .id(entity.getId())
                .doctorUserId(((User) doctorUser).getId())
                .doctorFullName(doctorUser.getFirstname() + " " + doctorUser.getLastname())
                .patientUserId(patientUser.getId())
                .patientFullName(patientUser.getFirstname() + " " + patientUser.getLastname())
                .date(entity.getDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .reason(entity.getReason())
                .build();
    }
}
