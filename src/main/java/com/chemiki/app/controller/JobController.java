package com.chemiki.app.controller;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.CreateJobRequestDTO;
import com.chemiki.app.dto.responseDto.JobDetailResponseDTO;
import com.chemiki.app.dto.responseDto.JobResponseDTO;
import com.chemiki.app.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping("/job")
    public ResponseEntity<ApiResponse<JobResponseDTO>> uploadJob(
            @Valid @RequestBody CreateJobRequestDTO request, // Changed from @ModelAttribute to @RequestBody (no files)
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<JobResponseDTO> response = jobService.uploadJob(request, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status;
            switch (errorCode) {
                case "UNVERIFIED_USER":
                    status = HttpStatus.FORBIDDEN;
                    break;
                case "INVALID_INPUT":
                    status = HttpStatus.BAD_REQUEST;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
            return ResponseEntity.status(status).body(response);
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobResponseDTO>>> getAllJobs(
            @RequestParam(required = false) String category) {
        ApiResponse<List<JobResponseDTO>> response = jobService.getAllJobs( );
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("JOB_NOT_FOUND") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    @GetMapping("/job/{id}")
    public ResponseEntity<ApiResponse<JobDetailResponseDTO>> getJobDetail(
            @PathVariable Long id) {
        ApiResponse<JobDetailResponseDTO> response = jobService.getJobDetail(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("JOB_NOT_FOUND") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    @DeleteMapping("/job/{id}")
    public ResponseEntity<ApiResponse<Void>> softDeleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Void> response = jobService.softDeleteJob(id, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("JOB_NOT_FOUND") ? HttpStatus.NOT_FOUND :
                    errorCode.equals("UNAUTHORIZED") ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    @PatchMapping("/job/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> updateJobAvailability(
            @PathVariable Long id,
            @RequestParam boolean isOpen,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<Void> response = jobService.updateJobAvailability(id, userId, isOpen);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("JOB_NOT_FOUND") ? HttpStatus.NOT_FOUND :
                    errorCode.equals("UNAUTHORIZED") ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<ApiResponse<List<JobResponseDTO>>> getMyJobs(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((com.chemiki.app.config.CustomUserDetails) userDetails).getUser().getId();
        ApiResponse<List<JobResponseDTO>> response = jobService.getMyJobs(userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            String errorCode = response.getErrorCode();
            HttpStatus status = errorCode.equals("JOB_NOT_FOUND") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }
}