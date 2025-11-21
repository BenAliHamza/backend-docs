package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.doctor.ActDto;
import tn.esprit.docsbackend.entities.Act;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.Specialty;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.mappers.ActMapper;
import tn.esprit.docsbackend.repositories.ActRepository;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.SpecialtyRepository;
import tn.esprit.docsbackend.services.ActService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActServiceImpl implements ActService {

    private final ActRepository actRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SpecialtyRepository specialtyRepository;
    private final ActMapper actMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ActDto> getActsForCurrentDoctor() {
        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile profile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        List<Act> acts = actRepository.findByDoctorIdAndDeletedFalse(profile.getId());

        return acts.stream()
                .map(actMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActDto> getActsBySpecialtyId(Long specialtyId) {
        if (specialtyId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "specialtyId is required");
        }

        User currentUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        DoctorProfile doctor = doctorProfileRepository
                .findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        Specialty specialty = specialtyRepository.findByIdAndDeletedFalse(specialtyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specialty not found"));

        if (doctor.getSpecialty() == null || !doctor.getSpecialty().getId().equals(specialty.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor is not associated with this specialty");
        }

        List<Act> acts = actRepository.findByDoctorIdAndSpecialtyIdAndDeletedFalse(doctor.getId(), specialtyId);

        return acts.stream()
                .map(actMapper::toDto)
                .collect(Collectors.toList());
    }
}
