package com.aluminibackend.api.module.alumnievent.repository;

import com.aluminibackend.api.module.alumnievent.entity.AlumniEvent;
import com.aluminibackend.api.module.alumnievent.enums.EventStatus;
import com.aluminibackend.api.module.alumnievent.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlumniEventRepository extends JpaRepository<AlumniEvent, Long> {

    List<AlumniEvent> findByEventType(EventType eventType);

    List<AlumniEvent> findByStatus(EventStatus status);

    List<AlumniEvent> findByTargetCohort(String targetCohort);
}