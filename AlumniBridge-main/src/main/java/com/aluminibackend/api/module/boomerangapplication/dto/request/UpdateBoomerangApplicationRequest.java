package com.aluminibackend.api.module.boomerangapplication.dto.request;

import com.aluminibackend.api.module.boomerangapplication.enums.BoomerangApplicationStatus;
import lombok.Data;

@Data
public class UpdateBoomerangApplicationRequest {

    private String jobRequisitionId;

    private Double previousTenureYears;

    private Long recruiterId;

    private BoomerangApplicationStatus status;
}