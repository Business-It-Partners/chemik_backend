package com.chemiki.app.service;

import com.chemiki.app.dto.ApiResponse;
import com.chemiki.app.dto.requestDto.CreateJobRequestDTO;
import com.chemiki.app.dto.responseDto.JobDetailResponseDTO;
import com.chemiki.app.dto.responseDto.JobResponseDTO;
import com.chemiki.app.model.Job;
import com.chemiki.app.model.User;
import com.chemiki.app.repository.JobRepository;
import com.chemiki.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {
    private final FCMService fcmService; // 🔥 NEW: Added FCM service

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ApiResponse<JobResponseDTO> uploadJob(CreateJobRequestDTO request, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.isInstitutionalUser() && !user.isVerified()) {
                return ApiResponse.error("Verification required for institutional users", "UNVERIFIED_USER");
            }

            // Validate required fields
            if (request.getTitle() == null || request.getSalary() == null ||
                    request.getJobType() == null || request.getLocation() == null ||
                    request.getContactInfo() == null) {
                return ApiResponse.error("All required fields (title, salary, job type, location, contact info) are mandatory", "INVALID_INPUT");
            }

            Job job = new Job();
            job.setTitle(request.getTitle());
            job.setDescription(request.getDescription());
            job.setSalary(request.getSalary());
            job.setJobType(request.getJobType());
            job.setLocation(request.getLocation());
            job.setContactNo(request.getContactInfo());
            job.setCategory(""); // Set as empty string for now as requested
            job.setOpen(true);
            job.setUserId(userId);
            job.setCreatedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            job = jobRepository.save(job);
// 🔥 NEW: Send broadcast notification for job posting
            String title = "💼 New Job: " + job.getTitle();
            String formattedSalary = formatSalary(job.getSalary());
            String body = job.getLocation() + " • " + job.getJobType() + " • Rs. " + formattedSalary;

            fcmService.sendBroadcastNotification(userId, title, body, "JOB", job.getId());
            JobResponseDTO responseDTO = new JobResponseDTO();
            responseDTO.setId(job.getId());
            responseDTO.setTitle(job.getTitle());
            responseDTO.setDescription(job.getDescription());
            responseDTO.setSalary(job.getSalary());
            responseDTO.setCategory(job.getCategory());
            responseDTO.setLocation(job.getLocation());
            responseDTO.setContactNo(job.getContactNo());
            responseDTO.setJobType(job.getJobType());
            responseDTO.setOpen(job.isOpen());
            responseDTO.setUsername(user.getUsername());
            responseDTO.setCreatedAt(job.getCreatedAt());
            responseDTO.setUpdatedAt(job.getUpdatedAt());
            responseDTO.setProfilePicture(user.getProfilePhotoUrl());

            return ApiResponse.success(responseDTO, "Job uploaded successfully");
        } catch (Exception e) {

            return ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<List<JobResponseDTO>> getAllJobs() {
        try {
            List<Job> jobs = jobRepository.findAllOpenOrderByCreatedAtDesc();
            if (jobs.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No jobs available");
            }

            List<JobResponseDTO> responseDTOs = jobs.stream().map(job -> {
                // Get user details for username
                User owner = userRepository.findById(job.getUserId()).orElse(null);

                JobResponseDTO dto = new JobResponseDTO();
                dto.setId(job.getId());
                dto.setTitle(job.getTitle());
                dto.setDescription(job.getDescription());
                dto.setSalary(job.getSalary());
                dto.setCategory(job.getCategory());
                dto.setLocation(job.getLocation());
                dto.setContactNo(job.getContactNo());
                dto.setJobType(job.getJobType());
                dto.setOpen(job.isOpen());
                dto.setUserId(job.getUserId());
                dto.setUsername(owner != null ? owner.getUsername() : "Unknown");
                dto.setCreatedAt(job.getCreatedAt());
                dto.setUpdatedAt(job.getUpdatedAt());
                dto.setProfilePicture(owner.getProfilePhotoUrl() );
                return dto;
            }).collect(Collectors.toList());
            return ApiResponse.success(responseDTOs, "Jobs retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "JOB_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving jobs", "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<JobDetailResponseDTO> getJobDetail(Long jobId) {
        try {
            Job job = jobRepository.findById(jobId)
                    .filter(j -> !j.isDeleted())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found with id: " + jobId));

            User owner = userRepository.findById(job.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found for job id: " + jobId));

            JobDetailResponseDTO dto = new JobDetailResponseDTO();
            dto.setId(job.getId());
            dto.setTitle(job.getTitle());
            dto.setDescription(job.getDescription());
            dto.setSalary(job.getSalary());
            dto.setCategory(job.getCategory());
            dto.setLocation(job.getLocation());
            dto.setContactNo(job.getContactNo());
            dto.setJobType(job.getJobType());
            dto.setOpen(job.isOpen());
            dto.setUserId(job.getUserId());
            dto.setUsername(owner.getUsername());
            dto.setOwnerPhoneNumber(owner.getPhoneNumber());
            dto.setCreatedAt(job.getCreatedAt());
            dto.setUpdatedAt(job.getUpdatedAt());
            dto.setProfilePicture(owner.getProfilePhotoUrl()); // Set profile picture URL

            return ApiResponse.success(dto, "Job details retrieved successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "JOB_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving job details", "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<Void> softDeleteJob(Long jobId, Long userId) {
        try {
            Job job = jobRepository.findByIdAndDeletedFalse(jobId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found with id: " + jobId));

            if (!job.getUserId().equals(userId)) {
                return ApiResponse.error("You are not authorized to delete this job", "UNAUTHORIZED");
            }

            job.setDeleted(true);
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);

            return ApiResponse.success(null, "Job deleted successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "JOB_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while deleting the job", "INTERNAL_SERVER_ERROR");
        }
    }

    // Close job (set isOpen = false)
    public ApiResponse<JobDetailResponseDTO> closeJob(Long jobId, Long userId) {
        try {
            Job job = jobRepository.findByIdAndDeletedFalse(jobId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found with id: " + jobId));

            if (!job.getUserId().equals(userId)) {
                return ApiResponse.error("You are not authorized to close this job", "UNAUTHORIZED");
            }

            job.setOpen(false);
            job.setUpdatedAt(LocalDateTime.now());
            job = jobRepository.save(job);

            User owner = userRepository.findById(job.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found for job id: " + jobId));

            JobDetailResponseDTO dto = new JobDetailResponseDTO();
            dto.setId(job.getId());
            dto.setTitle(job.getTitle());
            dto.setDescription(job.getDescription());
            dto.setSalary(job.getSalary());
            dto.setCategory(job.getCategory());
            dto.setLocation(job.getLocation());
            dto.setContactNo(job.getContactNo());
            dto.setJobType(job.getJobType());
            dto.setOpen(job.isOpen());
            dto.setUserId(job.getUserId());
            dto.setUsername(owner.getUsername());
            dto.setOwnerPhoneNumber(owner.getPhoneNumber());
            dto.setCreatedAt(job.getCreatedAt());
            dto.setUpdatedAt(job.getUpdatedAt());

            return ApiResponse.success(dto, "Job closed successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "JOB_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while closing the job", "INTERNAL_SERVER_ERROR");
        }
    }
    // Replace your existing getMyJobs method with this generic method in JobService.java

    /**
     * Get jobs by any user ID (generic method)
     */
    public ApiResponse<List<JobResponseDTO>> getUserJobs(Long userId) {
        try {
            // Check if user exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ApiResponse.error("User not found with id: " + userId, "USER_NOT_FOUND");
            }

            List<Job> jobs = jobRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

            // If no jobs found, return empty list instead of error
            if (jobs.isEmpty()) {
                return ApiResponse.success(new ArrayList<>(), "No jobs found for user");
            }

            List<JobResponseDTO> responseDTOs = jobs.stream().map(job -> {
                JobResponseDTO dto = new JobResponseDTO();
                dto.setId(job.getId());
                dto.setTitle(job.getTitle());
                dto.setDescription(job.getDescription());
                dto.setSalary(job.getSalary());
                dto.setCategory(job.getCategory());
                dto.setLocation(job.getLocation());
                dto.setContactNo(job.getContactNo());
                dto.setJobType(job.getJobType());
                dto.setOpen(job.isOpen());
                dto.setUserId(job.getUserId());
                dto.setUsername(user.getUsername());
                dto.setCreatedAt(job.getCreatedAt());
                dto.setUpdatedAt(job.getUpdatedAt());
                return dto;
            }).collect(Collectors.toList());

            return ApiResponse.success(responseDTOs, "Jobs retrieved successfully for user: " + user.getUsername());
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving user jobs", "INTERNAL_SERVER_ERROR");
        }
    }

    // Helper method to format salary with commas
    private String formatSalary(Double salary) {
        if (salary == null) return "0";

        // Convert to long to remove decimal places for whole numbers
        long salaryLong = salary.longValue();

        // Format with commas (Indian number format)
        return String.format("%,d", salaryLong);
    }
}