package com.aluminibackend.audit.repository;

import com.aluminibackend.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEmployeeId(String employeeId);

    List<AuditLog> findByModule(String module);
}
