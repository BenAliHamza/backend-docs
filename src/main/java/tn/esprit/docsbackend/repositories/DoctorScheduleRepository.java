package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.DoctorSchedule;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctorIdAndDeletedFalseAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

    List<DoctorSchedule> findByDoctorIdAndDeletedFalseAndActiveTrueAndDayOfWeekOrderByStartTimeAsc(
            Long doctorId,
            DayOfWeek dayOfWeek
    );

    boolean existsByDoctorIdAndDeletedFalse(Long doctorId);
}
