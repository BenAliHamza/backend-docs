package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.dto.doctor.SpecialtyDto;
import tn.esprit.docsbackend.entities.Specialty;
import tn.esprit.docsbackend.mappers.SpecialtyMapper;
import tn.esprit.docsbackend.repositories.SpecialtyRepository;
import tn.esprit.docsbackend.services.SpecialtyService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyDto> getAllSpecialties() {
        // We currently return all specialties. In the future, if you start
        // doing soft-deletes on specialties, we can add a `findByDeletedFalse`
        // method to the repository and filter here.
        List<Specialty> specialties = specialtyRepository.findAll();

        return specialties.stream()
                .map(specialtyMapper::toDto)
                .collect(Collectors.toList());
    }
}
