package com.aluminibackend.api.module.boomerangapplication.service;

import com.aluminibackend.api.module.boomerangapplication.dto.request.CreateBoomerangApplicationRequest;
import com.aluminibackend.api.module.boomerangapplication.dto.request.UpdateBoomerangApplicationRequest;
import com.aluminibackend.api.module.boomerangapplication.dto.response.BoomerangApplicationResponse;
import com.aluminibackend.api.module.boomerangapplication.enums.BoomerangApplicationStatus;

import java.util.List;

public interface BoomerangApplicationService {

    BoomerangApplicationResponse createApplication(
            CreateBoomerangApplicationRequest request
    );

    BoomerangApplicationResponse getApplicationById(Long applicationId);

    List<BoomerangApplicationResponse> getAllApplications();

    List<BoomerangApplicationResponse> getApplicationsByAlumni(Long alumniId);

    List<BoomerangApplicationResponse> getApplicationsByRecruiter(Long recruiterId);

    List<BoomerangApplicationResponse> getApplicationsByJob(String jobRequisitionId);

    List<BoomerangApplicationResponse> getApplicationsByStatus(
            BoomerangApplicationStatus status
    );

    BoomerangApplicationResponse updateApplication(
            Long applicationId,
            UpdateBoomerangApplicationRequest request
    );

    BoomerangApplicationResponse updateStatus(
            Long applicationId,
            BoomerangApplicationStatus status
    );

    void deleteApplication(Long applicationId);
}