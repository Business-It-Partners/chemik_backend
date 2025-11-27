package com.chemiki.app.dto.responseDto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class InstitutionalUserListResponseDTO {
    private List<InstitutionalUserDTO> users;
    private String message;
    private int totalCount;

    @Data
    public static class InstitutionalUserDTO {
        private Long id;
        private String username;
        private String email;
        private String phoneNumber;
        private String profilePhotoUrl;
        private String dateOfBirth;
        private String district;
        private String palika;
        private String ward;
        private String institutionCategory;
        private Instant createdAt;
    }
}