package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.Act;

import java.util.List;

public interface ActRepository extends JpaRepository<Act, Long> {

    List<Act> findByDoctorIdAndDeletedFalse(Long doctorId);

    List<Act> findByDoctorIdAndSpecialtyIdAndDeletedFalse(Long doctorId, Long specialtyId);
}
