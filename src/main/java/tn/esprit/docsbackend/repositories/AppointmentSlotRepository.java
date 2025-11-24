// src/main/java/tn/esprit/docsbackend/repositories/AppointmentSlotRepository.java
package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.AppointmentSlot;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.enums.SlotStatus;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByDoctorProfileAndStartDateTimeBetween(
            DoctorProfile doctorProfile,
            LocalDateTime from,
            LocalDateTime to
    );

    List<AppointmentSlot> findByDoctorProfileAndStartDateTimeBetweenAndStatus(
            DoctorProfile doctorProfile,
            LocalDateTime from,
            LocalDateTime to,
            SlotStatus status
    );

    // 🔹 NEW: used to remove old non-booked slots when doctor changes availability
   List<AppointmentSlot> findByDoctorProfileAndStatusNot(DoctorProfile doctorProfile, SlotStatus status);

    Optional<AppointmentSlot> findByIdAndStatus(Long id, SlotStatus status);  // <-- ADD THIS
}

