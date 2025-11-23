// src/main/java/tn/esprit/docsbackend/repositories/AppointmentSlotRepository.java
package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.AppointmentSlot;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.enums.SlotStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByDoctorProfileAndStartDateTimeBetweenAndStatus(
            DoctorProfile doctorProfile,
            LocalDateTime from,
            LocalDateTime to,
            SlotStatus status
    );

    List<AppointmentSlot> findByDoctorProfileAndStartDateTimeBetween(
            DoctorProfile doctorProfile,
            LocalDateTime from,
            LocalDateTime to
    );

    List<AppointmentSlot> findByPatientProfileAndStartDateTimeAfter(
            PatientProfile patientProfile,
            LocalDateTime after
    );

    Optional<AppointmentSlot> findByIdAndStatus(Long id, SlotStatus status);
}
