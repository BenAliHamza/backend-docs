package tn.esprit.docsbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.docsbackend.dto.appointment.AppointmentCreateRequest;
import tn.esprit.docsbackend.dto.appointment.AppointmentDto;
import tn.esprit.docsbackend.entities.Appointment;
import tn.esprit.docsbackend.entities.DoctorProfile;
import tn.esprit.docsbackend.entities.PatientProfile;
import tn.esprit.docsbackend.entities.User;
import tn.esprit.docsbackend.entities.enums.AppointmentStatus;
import tn.esprit.docsbackend.entities.enums.Role;
import tn.esprit.docsbackend.mappers.AppointmentMapper;
import tn.esprit.docsbackend.repositories.AppointmentRepository;
import tn.esprit.docsbackend.repositories.DoctorProfileRepository;
import tn.esprit.docsbackend.repositories.PatientProfileRepository;
import tn.esprit.docsbackend.repositories.UserRepository;
import tn.esprit.docsbackend.services.AppointmentService;
import tn.esprit.docsbackend.utils.SecurityUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    public AppointmentDto requestAppointmentAsPatient(AppointmentCreateRequest request) {
        User currentPatientUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        PatientProfile patientProfile = patientProfileRepository
                .findByUserIdAndDeletedFalse(currentPatientUser.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Patient profile not found"));

        User doctorUser = userRepository.findById(request.getDoctorUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Doctor user not found"));

        if (doctorUser.getRole() != Role.DOCTOR) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Target user is not a doctor");
        }

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserIdAndDeletedFalse(doctorUser.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Doctor profile not found"));

        // TODO plus tard : vérifier les conflits d’horaires
        Appointment appointment = Appointment.builder()
                .doctor(doctorProfile)
                .patient(patientProfile)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(AppointmentStatus.PENDING)
                .reason(request.getReason())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getMyAppointmentsAsPatient() {
        User currentPatientUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.PATIENT);

        return appointmentRepository
                .findByPatientUserIdOrderByDateDesc(currentPatientUser.getId())
                .stream()
                .map(appointmentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getMyAppointmentsAsDoctor(AppointmentStatus status,
                                                          LocalDate from,
                                                          LocalDate to) {
        User currentDoctorUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        List<Appointment> appointments;

        if (from != null && to != null) {
            appointments = appointmentRepository
                    .findByDoctorUserIdAndDateBetweenOrderByDateAsc(
                            currentDoctorUser.getId(), from, to);
        } else if (status != null) {
            appointments = appointmentRepository
                    .findByDoctorUserIdAndStatusOrderByDateAsc(
                            currentDoctorUser.getId(), status);
        } else {
            appointments = appointmentRepository
                    .findByDoctorUserIdOrderByDateAsc(currentDoctorUser.getId());
        }

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .toList();
    }

    @Override
    public AppointmentDto acceptAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentForCurrentDoctorOrThrow(appointmentId);
        appointment.setStatus(AppointmentStatus.ACCEPTED);
        return appointmentMapper.toDto(appointment);
    }

    @Override
    public AppointmentDto rejectAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentForCurrentDoctorOrThrow(appointmentId);
        appointment.setStatus(AppointmentStatus.REJECTED);
        return appointmentMapper.toDto(appointment);
    }

    @Override
    public AppointmentDto completeAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentForCurrentDoctorOrThrow(appointmentId);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentMapper.toDto(appointment);
    }

    private Appointment getAppointmentForCurrentDoctorOrThrow(Long id) {
        User currentDoctorUser = SecurityUtils.getCurrentUserWithRoleOrThrow(Role.DOCTOR);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Appointment not found"));

        Long appointmentDoctorUserId = appointment.getDoctor().getUser().getId();
        if (!appointmentDoctorUserId.equals(currentDoctorUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not the owner of this appointment");
        }

        return appointment;
    }
}
