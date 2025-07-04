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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ApiResponse<JobResponseDTO> uploadJob(CreateJobRequestDTO request, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.isInstitutionalUser() && !user.isVerified()) {
                return ApiResponse.error("Verification required for institutional users", "UNVERIFIED_USER");
            }

            if (request.getTitle() == null || request.getSalary() == null || request.getPhoneNumber() == null) {
                return ApiResponse.error("All required fields (title, salary, phone number) are mandatory", "INVALID_INPUT");
            }

            Job job = new Job();
            job.setTitle(request.getTitle());
            job.setDescription(request.getDescription());
            job.setSalary(request.getSalary());
            job.setCategory(""); // Can be blank for now
            job.setOpen(true);
            job.setUserId(userId);
            job.setCreatedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            job = jobRepository.save(job);

            JobResponseDTO responseDTO = new JobResponseDTO();
            responseDTO.setId(job.getId());
            responseDTO.setTitle(job.getTitle());
            responseDTO.setDescription(job.getDescription());
            responseDTO.setSalary(job.getSalary());

            responseDTO.setCreatedAt(job.getCreatedAt());

            return ApiResponse.success(responseDTO, "Job uploaded successfully");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while uploading the job", "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<List<JobResponseDTO>> getAllJobs() {
        try {
            List<Job> jobs;

                jobs = jobRepository.findAllOpenOrderByCreatedAtDesc();
                if (jobs.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No jobs available");
                }

            List<JobResponseDTO> responseDTOs = jobs.stream().map(job -> {
                JobResponseDTO dto = new JobResponseDTO();
                dto.setId(job.getId());
                dto.setTitle(job.getTitle());
                dto.setDescription(job.getDescription());
                dto.setSalary(job.getSalary());

                dto.setCreatedAt(job.getCreatedAt());
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
            dto.setCreatedAt(job.getCreatedAt());
            dto.setOwnerUsername(owner.getUsername());
            dto.setOwnerPhoneNumber(owner.getPhoneNumber());

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

            return ApiResponse.success(null, "Job soft deleted successfully");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "JOB_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while deleting the job", "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<Void> updateJobAvailability(Long jobId, Long userId, boolean isOpen) {
        try {
            Job job = jobRepository.findByIdAndDeletedFalse(jobId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found with id: " + jobId));

            if (!job.getUserId().equals(userId)) {
                return ApiResponse.error("You are not authorized to update this job", "UNAUTHORIZED");
            }

            job.setOpen(isOpen);
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);

            return ApiResponse.success(null, isOpen ? "Job marked as open" : "Job marked as closed");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "JOB_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while updating job availability", "INTERNAL_SERVER_ERROR");
        }
    }

    public ApiResponse<List<JobResponseDTO>> getMyJobs(Long userId) {
        try {
            List<Job> jobs = jobRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
            if (jobs.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No jobs found for user id: " + userId);
            }
            List<JobResponseDTO> responseDTOs = jobs.stream().map(job -> {
                JobResponseDTO dto = new JobResponseDTO();
                dto.setId(job.getId());
                dto.setTitle(job.getTitle());
                dto.setDescription(job.getDescription());
                dto.setSalary(job.getSalary());

                dto.setCreatedAt(job.getCreatedAt());
                return dto;
            }).collect(Collectors.toList());
            return ApiResponse.success(responseDTOs, "Jobs retrieved successfully for user");
        } catch (ResponseStatusException e) {
            return ApiResponse.error(e.getReason(), "JOB_NOT_FOUND");
        } catch (Exception e) {
            return ApiResponse.error("An error occurred while retrieving user jobs", "INTERNAL_SERVER_ERROR");
        }
    }
}