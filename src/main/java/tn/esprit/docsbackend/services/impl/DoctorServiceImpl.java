package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.doctor.*;
import tn.esprit.docsbackend.dto.patient.PatientProfileDto;
import tn.esprit.docsbackend.entities.*;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.entities.enums.UserStatus;
import tn.esprit.docsbackend.mappers.ActMapper;
import tn.esprit.docsbackend.mappers.DoctorProfileMapper;
import tn.esprit.docsbackend.mappers.PatientProfileMapper;
import tn.esprit.docsbackend.repositories.*;
import tn.esprit.docsbackend.services.AppointmentService;
import tn.esprit.docsbackend.services.DoctorService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final ActRepository actRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorProfileMapper doctorProfileMapper;
    private final PatientProfileMapper patientProfileMapper;
    private final ActMapper actMapper;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileDto getCurrentDoctorProfile() {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();

        if (currentUser.getRole() != Role.DOCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors can access this resource");
        }

        DoctorProfile profile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        return doctorProfileMapper.toDto(profile);
    }

    /**
     * Update the profile of the currently authenticated doctor (partial update).
     * On first successful update, marks user.isFirstLogin = false.
     */
    @Override
    @Transactional
    public DoctorProfileDto updateCurrentDoctorProfile(DoctorProfileUpdateRequest request) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile profile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        // Apply the partial update via mapper
        doctorProfileMapper.updateEntityFromRequest(request, profile);

        DoctorProfile saved = doctorProfileRepository.save(profile);

        // If this is the first time completing/updating profile, flip the flag
        if (Boolean.TRUE.equals(currentUser.getIsFirstLogin())) {
            currentUser.setIsFirstLogin(false);
            userRepository.save(currentUser);
        }

        return doctorProfileMapper.toDto(saved);
    }

    /**
     * Returns the list of patients linked to the currently authenticated doctor.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PatientProfileDto> getPatientsOfCurrentDoctor() {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile profile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        return profile.getPatients().stream()
                .map(patientProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single patient (by patient user id) linked to the currently authenticated doctor.
     */
    @Override
    @Transactional(readOnly = true)
    public PatientProfileDto getPatientOfCurrentDoctor(Long patientUserId) {
        if (patientUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientUserId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(patientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        Set<PatientProfile> doctorPatients =
                doctorProfile.getPatients() != null ? doctorProfile.getPatients() : new HashSet<>();

        if (doctorPatients.isEmpty() || !doctorPatients.contains(patientProfile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor is not linked to this patient");
        }

        return patientProfileMapper.toDto(patientProfile);
    }

    @Override
    @Transactional
    public void addPatientToCurrentDoctor(Long patientUserId) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(patientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        Set<PatientProfile> doctorPatients =
                doctorProfile.getPatients() != null ? doctorProfile.getPatients() : new HashSet<>();
        Set<DoctorProfile> patientDoctors =
                patientProfile.getDoctors() != null ? patientProfile.getDoctors() : new HashSet<>();

        if (doctorPatients.contains(patientProfile)) {
            doctorProfile.setPatients(doctorPatients);
            patientProfile.setDoctors(patientDoctors);
            return;
        }

        doctorPatients.add(patientProfile);
        patientDoctors.add(doctorProfile);

        doctorProfile.setPatients(doctorPatients);
        patientProfile.setDoctors(patientDoctors);

        doctorProfileRepository.save(doctorProfile);
    }

    @Override
    @Transactional
    public void removePatientFromCurrentDoctor(Long patientUserId) {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(patientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        Set<PatientProfile> doctorPatients =
                doctorProfile.getPatients() != null ? doctorProfile.getPatients() : new HashSet<>();
        Set<DoctorProfile> patientDoctors =
                patientProfile.getDoctors() != null ? patientProfile.getDoctors() : new HashSet<>();

        if (doctorPatients.remove(patientProfile)) {
            patientDoctors.remove(doctorProfile);

            doctorProfile.setPatients(doctorPatients);
            patientProfile.setDoctors(patientDoctors);

            doctorProfileRepository.save(doctorProfile);
        }
    }

    @Override
    @Transactional
    public DoctorProfileDto setupPracticeForCurrentDoctor(Long specialtyId, List<Long> actIds) {
        if (specialtyId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "specialtyId is required");
        }
        if (actIds == null || actIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one actId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        Specialty specialty = specialtyRepository.findByIdAndDeletedFalse(specialtyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specialty not found"));

        // For onboarding: set the single main specialty
        doctorProfile.setSpecialty(specialty);

        // Load acts by id
        List<Act> acts = actRepository.findAllById(actIds);

        if (acts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No acts found for given ids");
        }

        int distinctRequestedIds = new java.util.HashSet<>(actIds).size();
        if (acts.size() != distinctRequestedIds) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some act ids are invalid");
        }

        for (Act act : acts) {
            if (act == null || act.isDeleted()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One of the acts is not available");
            }
            // We no longer force acts to belong to this doctor or specialty here,
            // because the doctor is choosing from a global catalog seeded by specialty.
        }

        // Replace the doctor's acts with the selected ones (onboarding setup)
        java.util.Set<Act> doctorActs = doctorProfile.getActs() != null
                ? doctorProfile.getActs()
                : new java.util.HashSet<>();
        doctorActs.clear();
        doctorActs.addAll(acts);
        doctorProfile.setActs(doctorActs);

        DoctorProfile saved = doctorProfileRepository.save(doctorProfile);

        return doctorProfileMapper.toDto(saved);
    }

    /**
     * Search doctors with optional filters.
     * This is a simple in-memory filter over all doctor profiles for now.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DoctorSearchResultDto> searchDoctors(
            String query,
            Long specialtyId,
            String city,
            String country,
            Boolean teleconsultationEnabled,
            Boolean acceptingNewPatients
    ) {
        List<DoctorProfile> doctors = doctorProfileRepository.findAll();

        return doctors.stream()
                .filter(doctor -> doctor != null && !doctor.isDeleted())
                .filter(doctor -> {
                    User u = doctor.getUser();
                    return u != null
                            && !u.isDeleted()
                            && u.getStatus() == UserStatus.ACTIVE
                            && u.getRole() == Role.DOCTOR;
                })
                .filter(doctor -> {
                    if (specialtyId == null) return true;
                    Specialty sp = doctor.getSpecialty();
                    return sp != null && specialtyId.equals(sp.getId());
                })
                .filter(doctor -> {
                    if (city == null || city.isBlank()) return true;
                    String docCity = doctor.getCity();
                    return docCity != null && docCity.equalsIgnoreCase(city);
                })
                .filter(doctor -> {
                    if (country == null || country.isBlank()) return true;
                    String docCountry = doctor.getCountry();
                    return docCountry != null && docCountry.equalsIgnoreCase(country);
                })
                .filter(doctor -> {
                    if (teleconsultationEnabled == null) return true;
                    Boolean flag = doctor.getTeleconsultationEnabled();
                    return flag != null && flag.equals(teleconsultationEnabled);
                })
                .filter(doctor -> {
                    if (acceptingNewPatients == null) return true;
                    Boolean flag = doctor.getAcceptsNewPatients();
                    return flag != null && flag.equals(acceptingNewPatients);
                })
                .filter(doctor -> {
                    if (query == null || query.isBlank()) {
                        return true;
                    }
                    String q = query.toLowerCase().trim();

                    User u = doctor.getUser();
                    String first = (u != null && u.getFirstname() != null) ? u.getFirstname().toLowerCase() : "";
                    String last = (u != null && u.getLastname() != null) ? u.getLastname().toLowerCase() : "";

                    Specialty sp = doctor.getSpecialty();
                    String specName = (sp != null && sp.getName() != null) ? sp.getName().toLowerCase() : "";

                    return first.contains(q) || last.contains(q) || specName.contains(q);
                })
                .map(this::toSearchResultDto)
                .collect(Collectors.toList());
    }

    /**
     * Public profile view for a doctor by doctor profile id.
     */
    @Override
    @Transactional(readOnly = true)
    public DoctorPublicProfileDto getDoctorPublicProfile(Long doctorId) {
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Doctor profile with id=" + doctorId + " not found"
                ));

        return toPublicProfileDto(doctor);
    }

    // ----------------- Mapping helpers -----------------

    private DoctorSearchResultDto toSearchResultDto(DoctorProfile doctor) {
        User user = doctor.getUser();
        Specialty specialty = doctor.getSpecialty();

        return DoctorSearchResultDto.builder()
                .doctorId(doctor.getId())
                .userId(user != null ? user.getId() : null)
                .firstName(user != null ? user.getFirstname() : null)
                .lastName(user != null ? user.getLastname() : null)
                .specialtyId(specialty != null ? specialty.getId() : null)
                .specialtyName(specialty != null ? specialty.getName() : null)
                .city(doctor.getCity())
                .country(doctor.getCountry())
                .profileImageUrl(user != null ? user.getProfileImage() : null)
                .teleconsultationEnabled(doctor.getTeleconsultationEnabled())
                .acceptingNewPatients(doctor.getAcceptsNewPatients())
                .build();
    }

    private DoctorPublicProfileDto toPublicProfileDto(DoctorProfile doctor) {
        User user = doctor.getUser();
        Specialty specialty = doctor.getSpecialty();

        List<tn.esprit.docsbackend.dto.doctor.ActDto> actDtos = doctor.getActs().stream()
                .filter(act -> act != null && !act.isDeleted())
                .map(actMapper::toDto)
                .collect(Collectors.toList());

        return DoctorPublicProfileDto.builder()
                .doctorId(doctor.getId())
                .userId(user != null ? user.getId() : null)
                .firstName(user != null ? user.getFirstname() : null)
                .lastName(user != null ? user.getLastname() : null)
                .profileImageUrl(user != null ? user.getProfileImage() : null)
                .specialtyId(specialty != null ? specialty.getId() : null)
                .specialtyName(specialty != null ? specialty.getName() : null)
                .city(doctor.getCity())
                .country(doctor.getCountry())
                .clinicAddress(doctor.getClinicAddress())
                .bio(doctor.getBio())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .consultationFee(doctor.getConsultationFee())
                .acceptingNewPatients(doctor.getAcceptsNewPatients())
                .teleconsultationEnabled(doctor.getTeleconsultationEnabled())
                .acts(actDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorHomeStatsDto getHomeStatsForCurrentDoctor() {

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        Long doctorId = doctorProfile.getId();

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Blocking statuses under the new enum
        List<AppointmentStatus> blockingStatuses = List.of(
                AppointmentStatus.PENDING,
                AppointmentStatus.ACCEPTED,
                AppointmentStatus.COMPLETED
        );

        // Fetch all doctor's appointments once (optimized)
        List<Appointment> all = appointmentRepository
                .findByDoctorIdAndDeletedFalseOrderByStartAtAsc(doctorId);

        // 1️⃣ Today's appointments
        long todayCount = all.stream()
                .filter(a -> a.getStartAt() != null)
                .filter(a -> a.getStartAt().toLocalDate().equals(today))
                .filter(a -> blockingStatuses.contains(a.getStatus()))
                .count();

        // 2️⃣ This week (Monday → Sunday)
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate sunday = monday.plusDays(6);

        long weekCount = all.stream()
                .filter(a -> a.getStartAt() != null)
                .filter(a -> {
                    LocalDate d = a.getStartAt().toLocalDate();
                    return !d.isBefore(monday) && !d.isAfter(sunday);
                })
                .filter(a -> blockingStatuses.contains(a.getStatus()))
                .count();

        // 3️⃣ Total unique patients
        long totalPatients = doctorProfile.getPatients().size();

        // 4️⃣ Next upcoming appointment (ONLY PENDING or ACCEPTED)
        Appointment next = all.stream()
                .filter(a -> a.getStartAt() != null)
                .filter(a -> a.getStartAt().isAfter(now))
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING
                        || a.getStatus() == AppointmentStatus.ACCEPTED)
                .findFirst()
                .orElse(null);

        String nextStart = (next != null) ? next.getStartAt().toString() : null;
        String nextPatientName = null;

        if (next != null && next.getPatient() != null && next.getPatient().getUser() != null) {
            User p = next.getPatient().getUser();
            nextPatientName = p.getFirstname() + " " + p.getLastname();
        }

        return DoctorHomeStatsDto.builder()
                .todayAppointments(todayCount)
                .weekAppointments(weekCount)
                .totalPatients(totalPatients)
                .nextAppointmentStart(nextStart)
                .nextAppointmentPatientName(nextPatientName)
                .build();
    }


}
