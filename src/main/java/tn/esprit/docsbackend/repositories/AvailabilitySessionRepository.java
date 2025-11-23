// src/main/java/tn/esprit/docsbackend/repositories/AvailabilitySessionRepository.java
package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.AvailabilitySession;
import tn.esprit.docsbackend.entities.DoctorProfile;

import java.util.List;

public interface AvailabilitySessionRepository extends JpaRepository<AvailabilitySession, Long> {

    List<AvailabilitySession> findByDoctorProfileAndDeletedFalse(DoctorProfile doctorProfile);
}
