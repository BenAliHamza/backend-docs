package tn.esprit.docsbackend.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.dto.indicator.PatientIndicatorDto;
import tn.esprit.docsbackend.entities.IndicatorType;
import tn.esprit.docsbackend.entities.PatientIndicator;

@Component
public class PatientIndicatorMapper {

    public PatientIndicatorDto toDto(PatientIndicator entity) {
        if (entity == null) {
            return null;
        }
        PatientIndicatorDto dto = new PatientIndicatorDto();
        dto.setId(entity.getId());
        dto.setNumericValue(entity.getNumericValue());
        dto.setTextValue(entity.getTextValue());
        dto.setMeasuredAt(entity.getMeasuredAt());
        dto.setNote(entity.getNote());

        IndicatorType type = entity.getIndicatorType();
        if (type != null) {
            dto.setIndicatorTypeId(type.getId());
            dto.setIndicatorCode(type.getCode());
            dto.setIndicatorName(type.getName());
            dto.setUnit(type.getUnit());
        }

        return dto;
    }
}
