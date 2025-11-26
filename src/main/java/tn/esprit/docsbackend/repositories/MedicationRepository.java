package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.Medication;

import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findByDeletedFalseAndActiveTrueOrderByNameAsc();

    List<Medication> findByDeletedFalseAndActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(String name);

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);
}
