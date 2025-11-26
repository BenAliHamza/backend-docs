package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.PrescriptionLine;

public interface PrescriptionLineRepository extends JpaRepository<PrescriptionLine, Long> {
}
