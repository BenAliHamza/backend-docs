package tn.esprit.docsbackend.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lightweight doctor row used in /api/doctors/search result list.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSearchResultDto {

    // You can treat this as the doctor profile id
    private Long doctorId;

    // Linked User id (for auth, images, etc.)
    private Long userId;

    private String firstName;
    private String lastName;

    private String specialtyName;
    private Long specialtyId;

    private String city;
    private String country;

    private String profileImageUrl;   // from User.profileImage

    private Boolean teleconsultationEnabled;
    private Boolean acceptingNewPatients;
}
