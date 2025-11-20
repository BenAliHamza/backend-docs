package tn.esprit.docsbackend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ApiErrorResponse {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    /**
     * Optional map of field -> validation error messages.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, List<String>> fieldErrors;
}
