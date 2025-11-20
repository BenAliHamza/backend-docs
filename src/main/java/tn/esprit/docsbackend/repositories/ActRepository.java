package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.docsbackend.entities.Act;

import java.util.List;

@Repository
public interface ActRepository extends JpaRepository<Act, Long> {

    /**
     * Returns all active (not soft-deleted) acts for a given doctor profile.
     */
    List<Act> findByDoctorIdAndDeletedFalse(Long doctorId);
}
