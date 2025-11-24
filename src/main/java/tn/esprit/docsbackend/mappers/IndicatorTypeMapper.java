package tn.esprit.docsbackend.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.docsbackend.dto.indicator.IndicatorTypeDto;
import tn.esprit.docsbackend.entities.IndicatorType;

@Component
public class IndicatorTypeMapper {

    public IndicatorTypeDto toDto(IndicatorType entity) {
        if (entity == null) {
            return null;
        }
        IndicatorTypeDto dto = new IndicatorTypeDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setUnit(entity.getUnit());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.getActive());
        return dto;
    }
}
