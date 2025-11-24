package tn.esprit.docsbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.docsbackend.entities.IndicatorType;

import java.util.List;
import java.util.Optional;

public interface IndicatorTypeRepository extends JpaRepository<IndicatorType, Long> {

    Optional<IndicatorType> findByIdAndDeletedFalse(Long id);

    Optional<IndicatorType> findByCodeAndDeletedFalse(String code);

    List<IndicatorType> findAllByActiveTrueAndDeletedFalse();
}
