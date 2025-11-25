package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientUserIdOrderByDateDesc(Long patientUserId);

    List<Appointment> findByDoctorUserIdOrderByDateAsc(Long doctorUserId);

    List<Appointment> findByDoctorUserIdAndStatusOrderByDateAsc(
            Long doctorUserId,
            AppointmentStatus status
    );

    List<Appointment> findByDoctorUserIdAndDateBetweenOrderByDateAsc(
            Long doctorUserId,
            LocalDate start,
            LocalDate end
    );

    /**
     * Cherche les RDV qui se chevauchent pour un docteur donné, à une date donnée,
     * en excluant certains statuts (CANCELLED, REJECTED, ...).
     *
     * Condition de chevauchement :
     *   (existing.startTime < newEnd) AND (existing.endTime > newStart)
     */
    @Query("""
        select a
        from Appointment a
        where a.doctor.user.id = :doctorUserId
          and a.date = :date
          and a.status not in :excludedStatuses
          and (a.startTime < :endTime and a.endTime > :startTime)
        """)
    List<Appointment> findConflictingAppointments(
            @Param("doctorUserId") Long doctorUserId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses
    );
}
