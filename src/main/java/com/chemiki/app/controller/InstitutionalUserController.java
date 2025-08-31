package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.RegisterRequestDTO;
import com.chemiki.app.dto.responseDto.InstitutionalUserResponseDTO;
import com.chemiki.app.service.InstitutionalUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutional-users")
@RequiredArgsConstructor
public class InstitutionalUserController {

    private final InstitutionalUserService institutionalUserService;

    /**
     * Get all institutional users for explore section
     * GET /api/institutional-users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<InstitutionalUserResponseDTO>>> getAllInstitutionalUsers(
            @AuthenticationPrincipal UserDetails userDetails) {
        ApiResponse<List<InstitutionalUserResponseDTO>> response = institutionalUserService.getAllInstitutionalUsers();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




     /**
     * Create a new institutional user directly, marking it as verified
     * POST /api/institutional-users
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InstitutionalUserResponseDTO>> createInstitutionalUser(
            @RequestBody RegisterRequestDTO request) {
        ApiResponse<InstitutionalUserResponseDTO> response = institutionalUserService.createInstitutionalUser(request);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}