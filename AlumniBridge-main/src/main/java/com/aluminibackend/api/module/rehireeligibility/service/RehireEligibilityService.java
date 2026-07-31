package com.aluminibackend.api.module.rehireeligibility.service;

import com.aluminibackend.api.module.rehireeligibility.dto.request.CreateRehireEligibilityRequest;
import com.aluminibackend.api.module.rehireeligibility.dto.request.UpdateRehireEligibilityRequest;
import com.aluminibackend.api.module.rehireeligibility.dto.response.RehireEligibilityResponse;
import com.aluminibackend.api.module.rehireeligibility.enums.EligibleForRehire;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;

import java.util.List;

public interface RehireEligibilityService {

    RehireEligibilityResponse createEligibility(CreateRehireEligibilityRequest request);

    RehireEligibilityResponse getEligibilityById(Long eligibilityId);

    RehireEligibilityResponse getEligibilityByAlumni(Long alumniId);

    List<RehireEligibilityResponse> getAllEligibilities();

    List<RehireEligibilityResponse> getByEligibleForRehire(
            EligibleForRehire eligibleForRehire
    );

    List<RehireEligibilityResponse> getByStatus(
            RehireEligibilityStatus status
    );

    List<RehireEligibilityResponse> getByReviewer(Long reviewedById);

    RehireEligibilityResponse updateEligibility(
            Long eligibilityId,
            UpdateRehireEligibilityRequest request
    );

    RehireEligibilityResponse markEligible(Long eligibilityId);

    RehireEligibilityResponse markIneligible(Long eligibilityId);

    RehireEligibilityResponse markUnderReview(Long eligibilityId);

    void deleteEligibility(Long eligibilityId);
}