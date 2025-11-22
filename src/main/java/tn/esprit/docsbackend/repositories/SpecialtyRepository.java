package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.docsbackend.entities.Specialty;

import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    /**
     * Used by seeder to avoid duplicates by name.
     */
    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    /**
     * Used when validating specialtyId for /specialties/{id}/acts.
     */
    Optional<Specialty> findByIdAndDeletedFalse(Long id);
}
