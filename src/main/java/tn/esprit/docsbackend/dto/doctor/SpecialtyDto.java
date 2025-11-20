package tn.esprit.docsbackend.dto.doctor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SpecialtyDto {

    private Long id;

    private String code;

    private String name;

    private String description;

    private Boolean active;
}
