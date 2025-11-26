package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.Prescription;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByDoctorIdAndPatientIdAndDeletedFalseOrderByStartDateDesc(Long doctorId, Long patientId);

    List<Prescription> findByPatientIdAndDeletedFalseOrderByStartDateDesc(Long patientId);
}
