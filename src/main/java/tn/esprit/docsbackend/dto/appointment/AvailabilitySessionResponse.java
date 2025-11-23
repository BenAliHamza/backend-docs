// src/main/java/tn/esprit/docsbackend/dto/appointment/AvailabilitySessionResponse.java
package tn.esprit.docsbackend.dto.appointment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AvailabilitySessionResponse {

    private Long id;
    private Integer generatedSlotsCount;
}
