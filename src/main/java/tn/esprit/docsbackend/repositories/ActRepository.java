package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.docsbackend.entities.Act;

import java.util.List;

@Repository
public interface ActRepository extends JpaRepository<Act, Long> {

    /**
     * Acts belonging to a specific doctor (used in /doctors/me/acts).
     */
    List<Act> findByDoctorIdAndDeletedFalse(Long doctorId);

    /**
     * Acts linked to a given specialty, regardless of doctor.
     * Used by /specialties/{id}/acts.
     */
    List<Act> findBySpecialtyIdAndDeletedFalse(Long specialtyId);
}
