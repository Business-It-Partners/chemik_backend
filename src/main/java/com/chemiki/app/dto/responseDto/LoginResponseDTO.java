// folder: com.chemiki.app.dto.responseDto
// purpose: DTO for login response data containing JWT token
package com.chemiki.app.dto.responseDto;
// Purpose: DTO for returning access and refresh tokens in the API response. Uses @Builder, @AllArgsConstructor, and @NoArgsConstructor for object construction.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
}