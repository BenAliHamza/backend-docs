package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.Specialty;

import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    Optional<Specialty> findByIdAndDeletedFalse(Long id);

    Optional<Specialty> findByCodeIgnoreCaseAndDeletedFalse(String code);
}
