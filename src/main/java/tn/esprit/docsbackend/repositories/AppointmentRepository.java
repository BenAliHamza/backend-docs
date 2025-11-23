package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientUserIdOrderByDateDesc(Long patientUserId);

    List<Appointment> findByDoctorUserIdOrderByDateAsc(Long doctorUserId);

    List<Appointment> findByDoctorUserIdAndStatusOrderByDateAsc(Long doctorUserId,
                                                                AppointmentStatus status);

    List<Appointment> findByDoctorUserIdAndDateBetweenOrderByDateAsc(
            Long doctorUserId,
            LocalDate start,
            LocalDate end
    );
}
