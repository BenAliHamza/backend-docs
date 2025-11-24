package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.indicator.IndicatorTypeDto;
import tn.esprit.docsbackend.dto.indicator.PatientIndicatorCreateRequest;
import tn.esprit.docsbackend.dto.indicator.PatientIndicatorDto;
import tn.esprit.docsbackend.entities.*;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.mappers.IndicatorTypeMapper;
import tn.esprit.docsbackend.mappers.PatientIndicatorMapper;
import tn.esprit.docsbackend.repositories.*;
import tn.esprit.docsbackend.services.PatientIndicatorService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientIndicatorServiceImpl implements PatientIndicatorService {

    private final IndicatorTypeRepository indicatorTypeRepository;
    private final PatientIndicatorRepository patientIndicatorRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    private final IndicatorTypeMapper indicatorTypeMapper;
    private final PatientIndicatorMapper patientIndicatorMapper;

    @Override
    @Transactional(readOnly = true)
    public List<IndicatorTypeDto> getAllIndicatorTypes() {
        return indicatorTypeRepository.findAllByActiveTrueAndDeletedFalse()
                .stream()
                .map(indicatorTypeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PatientIndicatorDto addIndicatorForCurrentPatient(PatientIndicatorCreateRequest request) {
        if (request == null || request.getIndicatorTypeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "indicatorTypeId is required");
        }

        if (request.getNumericValue() == null
                && (request.getTextValue() == null || request.getTextValue().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numericValue or textValue must be provided");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        IndicatorType type = indicatorTypeRepository.findByIdAndDeletedFalse(request.getIndicatorTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indicator type not found"));

        LocalDateTime measuredAt = request.getMeasuredAt() != null
                ? request.getMeasuredAt()
                : LocalDateTime.now();

        PatientIndicator entity = PatientIndicator.builder()
                .patientProfile(patientProfile)
                .indicatorType(type)
                .numericValue(request.getNumericValue())
                .textValue(request.getTextValue())
                .measuredAt(measuredAt)
                .note(request.getNote())
                .build();

        PatientIndicator saved = patientIndicatorRepository.save(entity);

        return patientIndicatorMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientIndicatorDto> getIndicatorsForCurrentPatient(Long indicatorTypeId,
                                                                    LocalDateTime from,
                                                                    LocalDateTime to) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        List<PatientIndicator> list = fetchForPatient(patientProfile.getId(), indicatorTypeId, from, to);
        return list.stream()
                .map(patientIndicatorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientIndicatorDto> getIndicatorsForPatientAsDoctor(Long patientUserId,
                                                                     Long indicatorTypeId,
                                                                     LocalDateTime from,
                                                                     LocalDateTime to) {
        if (patientUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientUserId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        User patientUser = userRepository.findById(patientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient user not found"));

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(patientUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        // Enforce that doctor is linked to this patient
        Set<PatientProfile> doctorPatients = doctorProfile.getPatients();
        if (doctorPatients == null || !doctorPatients.contains(patientProfile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor is not linked to this patient");
        }

        List<PatientIndicator> list = fetchForPatient(patientProfile.getId(), indicatorTypeId, from, to);
        return list.stream()
                .map(patientIndicatorMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Soft-delete a measurement belonging to the current patient.
     */
    @Override
    @Transactional
    public void deleteIndicatorForCurrentPatient(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indicator id is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        PatientIndicator indicator = patientIndicatorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found"));

        // Ensure not already deleted
        if (indicator.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found");
        }

        // Ownership check: must belong to current patient's profile
        if (indicator.getPatientProfile() == null
                || !patientProfile.getId().equals(indicator.getPatientProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this indicator");
        }

        indicator.setDeleted(true);
        patientIndicatorRepository.save(indicator);
    }

    private List<PatientIndicator> fetchForPatient(Long patientProfileId,
                                                   Long indicatorTypeId,
                                                   LocalDateTime from,
                                                   LocalDateTime to) {

        boolean hasRange = (from != null && to != null);

        if (indicatorTypeId == null && !hasRange) {
            return patientIndicatorRepository
                    .findByPatientProfileIdAndDeletedFalseOrderByMeasuredAtDesc(patientProfileId);
        }

        if (indicatorTypeId != null && !hasRange) {
            return patientIndicatorRepository
                    .findByPatientProfileIdAndIndicatorTypeIdAndDeletedFalseOrderByMeasuredAtDesc(
                            patientProfileId, indicatorTypeId);
        }

        // we have a time range
        LocalDateTime fromEffective = (from != null) ? from : LocalDateTime.MIN;
        LocalDateTime toEffective = (to != null) ? to : LocalDateTime.MAX;

        if (indicatorTypeId == null) {
            return patientIndicatorRepository
                    .findByPatientProfileIdAndDeletedFalseAndMeasuredAtBetweenOrderByMeasuredAtDesc(
                            patientProfileId, fromEffective, toEffective);
        }

        return patientIndicatorRepository
                .findByPatientProfileIdAndIndicatorTypeIdAndDeletedFalseAndMeasuredAtBetweenOrderByMeasuredAtDesc(
                        patientProfileId, indicatorTypeId, fromEffective, toEffective);
    }
}
