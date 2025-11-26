package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.medication.PrescriptionCreateRequest;
import tn.esprit.docsbackend.dto.medication.PrescriptionDto;
import tn.esprit.docsbackend.dto.medication.PrescriptionLineDto;
import tn.esprit.docsbackend.dto.medication.PrescriptionLineCreateRequest;
import tn.esprit.docsbackend.entities.*;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.entities.enums.UserStatus;
import tn.esprit.docsbackend.repositories.*;
import tn.esprit.docsbackend.services.PrescriptionService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionLineRepository prescriptionLineRepository;
    private final MedicationRepository medicationRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    // ---------------- Doctor side ----------------

    @Override
    @Transactional
    public PrescriptionDto createPrescriptionForCurrentDoctorPatient(Long patientUserId,
                                                                     PrescriptionCreateRequest request) {
        if (patientUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientUserId is required");
        }
        if (request == null || request.getLines() == null || request.getLines().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one prescription line is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate and endDate are required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be on or after startDate");
        }

        // current doctor
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        // patient user & profile
        User patientUser = userRepository.findById(patientUserId)
                .filter(u -> !u.isDeleted() && u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient user not found"));

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(patientUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        // ensure doctor <-> patient link (same logic as indicators)
        Set<PatientProfile> doctorPatients =
                Optional.ofNullable(doctorProfile.getPatients()).orElseGet(HashSet::new);
        if (!doctorPatients.contains(patientProfile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor is not linked to this patient");
        }

        // build prescription
        Prescription prescription = Prescription.builder()
                .doctor(doctorProfile)
                .patient(patientProfile)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .note(request.getNote())
                .build();

        Set<PrescriptionLine> lines = new HashSet<>();

        for (PrescriptionLineCreateRequest lineReq : request.getLines()) {
            if (lineReq.getMedicationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "medicationId is required in each line");
            }

            Medication medication = medicationRepository.findById(lineReq.getMedicationId())
                    .filter(m -> !m.isDeleted() && Boolean.TRUE.equals(m.getActive()))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Medication " + lineReq.getMedicationId() + " not found or inactive"
                    ));

            PrescriptionLine line = PrescriptionLine.builder()
                    .prescription(prescription)
                    .medication(medication)
                    .dosage(lineReq.getDosage())
                    .timesPerDay(lineReq.getTimesPerDay())
                    .instructions(lineReq.getInstructions())
                    .reminderEnabled(Boolean.FALSE) // default: patient can enable later
                    .build();

            lines.add(line);
        }

        prescription.setLines(lines);

        Prescription saved = prescriptionRepository.save(prescription);

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPrescriptionsForCurrentDoctorPatient(Long patientUserId,
                                                                         Boolean activeOnly) {
        if (patientUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientUserId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        User patientUser = userRepository.findById(patientUserId)
                .filter(u -> !u.isDeleted() && u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient user not found"));

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(patientUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        // enforce doctor-patient link
        Set<PatientProfile> doctorPatients =
                Optional.ofNullable(doctorProfile.getPatients()).orElseGet(HashSet::new);
        if (!doctorPatients.contains(patientProfile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor is not linked to this patient");
        }

        List<Prescription> list = prescriptionRepository
                .findByDoctorIdAndPatientIdAndDeletedFalseOrderByStartDateDesc(doctorProfile.getId(), patientProfile.getId());

        LocalDate today = LocalDate.now();
        return list.stream()
                .filter(p -> !p.isDeleted())
                .filter(p -> !Boolean.TRUE.equals(activeOnly) || isActiveOn(p, today))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionDto getPrescriptionForCurrentDoctor(Long prescriptionId) {
        if (prescriptionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prescriptionId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Prescription with id=" + prescriptionId + " not found"
                ));

        if (!doctorProfile.getId().equals(prescription.getDoctor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This prescription does not belong to current doctor");
        }

        return toDto(prescription);
    }

    @Override
    @Transactional
    public void deletePrescriptionForCurrentDoctor(Long prescriptionId) {
        if (prescriptionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prescriptionId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Prescription with id=" + prescriptionId + " not found"
                ));

        if (!doctorProfile.getId().equals(prescription.getDoctor().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This prescription does not belong to current doctor");
        }

        prescription.setDeleted(true);
        if (prescription.getLines() != null) {
            prescription.getLines().forEach(line -> line.setDeleted(true));
        }
        prescriptionRepository.save(prescription);
    }

    // ---------------- Patient side ----------------

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPrescriptionsForCurrentPatient(Boolean activeOnly) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);
        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        List<Prescription> list = prescriptionRepository
                .findByPatientIdAndDeletedFalseOrderByStartDateDesc(patientProfile.getId());

        LocalDate today = LocalDate.now();
        return list.stream()
                .filter(p -> !p.isDeleted())
                .filter(p -> !Boolean.TRUE.equals(activeOnly) || isActiveOn(p, today))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionDto getPrescriptionForCurrentPatient(Long prescriptionId) {
        if (prescriptionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prescriptionId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);
        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Prescription with id=" + prescriptionId + " not found"
                ));

        if (!patientProfile.getId().equals(prescription.getPatient().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This prescription does not belong to current patient");
        }

        return toDto(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionLineDto> getActiveLinesForCurrentPatient() {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);
        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        List<Prescription> list = prescriptionRepository
                .findByPatientIdAndDeletedFalseOrderByStartDateDesc(patientProfile.getId());

        LocalDate today = LocalDate.now();

        return list.stream()
                .filter(p -> !p.isDeleted())
                .filter(p -> isActiveOn(p, today))
                .flatMap(p -> Optional.ofNullable(p.getLines())
                        .orElseGet(HashSet::new)
                        .stream()
                        .filter(l -> !l.isDeleted()))
                .map(this::toLineDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PrescriptionLineDto updateReminderForCurrentPatientLine(Long lineId, Boolean reminderEnabled) {
        if (lineId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lineId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);
        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        PrescriptionLine line = prescriptionLineRepository.findById(lineId)
                .filter(l -> !l.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Prescription line with id=" + lineId + " not found"
                ));

        Prescription prescription = line.getPrescription();
        if (prescription == null
                || prescription.isDeleted()
                || prescription.getPatient() == null
                || !patientProfile.getId().equals(prescription.getPatient().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This line does not belong to current patient");
        }

        line.setReminderEnabled(reminderEnabled);
        PrescriptionLine saved = prescriptionLineRepository.save(line);

        return toLineDto(saved);
    }

    // ---------------- helpers ----------------

    private boolean isActiveOn(Prescription p, LocalDate date) {
        if (p.getStartDate() == null || p.getEndDate() == null) {
            return false;
        }
        return ( !p.getStartDate().isAfter(date) && !p.getEndDate().isBefore(date) );
    }

    private PrescriptionDto toDto(Prescription p) {
        DoctorProfile doctor = p.getDoctor();
        PatientProfile patient = p.getPatient();

        User doctorUser = doctor != null ? doctor.getUser() : null;
        User patientUser = patient != null ? patient.getUser() : null;

        List<PrescriptionLineDto> lineDtos = Optional.ofNullable(p.getLines())
                .orElseGet(HashSet::new)
                .stream()
                .filter(l -> !l.isDeleted())
                .map(this::toLineDto)
                .collect(Collectors.toList());

        return PrescriptionDto.builder()
                .id(p.getId())
                .doctorId(doctor != null ? doctor.getId() : null)
                .doctorUserId(doctorUser != null ? doctorUser.getId() : null)
                .doctorFirstName(doctorUser != null ? doctorUser.getFirstname() : null)
                .doctorLastName(doctorUser != null ? doctorUser.getLastname() : null)
                .patientId(patient != null ? patient.getId() : null)
                .patientUserId(patientUser != null ? patientUser.getId() : null)
                .patientFirstName(patientUser != null ? patientUser.getFirstname() : null)
                .patientLastName(patientUser != null ? patientUser.getLastname() : null)
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .note(p.getNote())
                .lines(lineDtos)
                .build();
    }

    private PrescriptionLineDto toLineDto(PrescriptionLine line) {
        Prescription p = line.getPrescription();
        Medication med = line.getMedication();

        return PrescriptionLineDto.builder()
                .id(line.getId())
                .prescriptionId(p != null ? p.getId() : null)
                .prescriptionStartDate(p != null ? p.getStartDate() : null)
                .prescriptionEndDate(p != null ? p.getEndDate() : null)
                .medicationId(med != null ? med.getId() : null)
                .medicationName(med != null ? med.getName() : null)
                .dosage(line.getDosage())
                .timesPerDay(line.getTimesPerDay())
                .instructions(line.getInstructions())
                .reminderEnabled(line.getReminderEnabled())
                .build();
    }
}
