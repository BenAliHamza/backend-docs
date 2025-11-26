package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Check overlap for a given doctor and status.
     * We consider an overlap if:
     *  new.startAt < existing.endAt AND new.endAt > existing.startAt
     */
    List<Appointment> findByDoctorIdAndDeletedFalseAndStatusAndStartAtLessThanAndEndAtGreaterThan(
            Long doctorId,
            AppointmentStatus status,
            LocalDateTime end,
            LocalDateTime start
    );

    List<Appointment> findByDoctorIdAndDeletedFalseAndStartAtBetweenOrderByStartAtAsc(
            Long doctorId,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Appointment> findByPatientIdAndDeletedFalseAndStartAtBetweenOrderByStartAtAsc(
            Long patientId,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Appointment> findByDoctorIdAndDeletedFalseOrderByStartAtAsc(Long doctorId);

    List<Appointment> findByPatientIdAndDeletedFalseOrderByStartAtAsc(Long patientId);
}
