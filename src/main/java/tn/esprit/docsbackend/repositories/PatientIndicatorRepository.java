package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.PatientIndicator;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientIndicatorRepository extends JpaRepository<PatientIndicator, Long> {

    /**
     * Measurements for one patient, optionally filtered by type and date range.
     */
    List<PatientIndicator> findByPatientProfileIdAndDeletedFalseOrderByMeasuredAtDesc(Long patientProfileId);

    List<PatientIndicator> findByPatientProfileIdAndIndicatorTypeIdAndDeletedFalseOrderByMeasuredAtDesc(
            Long patientProfileId,
            Long indicatorTypeId
    );

    List<PatientIndicator> findByPatientProfileIdAndDeletedFalseAndMeasuredAtBetweenOrderByMeasuredAtDesc(
            Long patientProfileId,
            LocalDateTime from,
            LocalDateTime to
    );

    List<PatientIndicator> findByPatientProfileIdAndIndicatorTypeIdAndDeletedFalseAndMeasuredAtBetweenOrderByMeasuredAtDesc(
            Long patientProfileId,
            Long indicatorTypeId,
            LocalDateTime from,
            LocalDateTime to
    );
}
