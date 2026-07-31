package com.aluminibackend.audit.service;

import com.aluminibackend.audit.dto.AuditLogResponse;
import com.aluminibackend.audit.dto.RecordAuditRequest;
import com.aluminibackend.audit.entity.AuditLog;
import com.aluminibackend.audit.mapper.AuditLogMapper;
import com.aluminibackend.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AuditLogMapper mapper;

    @InjectMocks private AuditLogServiceImpl service;

    @Test
    void record_persistsSnapshotFromRequest() {
        RecordAuditRequest request = new RecordAuditRequest();
        request.setEmployeeId("EMP007");
        request.setUserName("Anil Kumar");
        request.setAction("CREATE_REHIRE_ELIGIBILITY");
        request.setModule("REHIRE_ELIGIBILITY");

        service.record(request);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getEmployeeId()).isEqualTo("EMP007");
        assertThat(saved.getUserName()).isEqualTo("Anil Kumar");
        assertThat(saved.getAction()).isEqualTo("CREATE_REHIRE_ELIGIBILITY");
        assertThat(saved.getModule()).isEqualTo("REHIRE_ELIGIBILITY");
    }

    @Test
    void getLogsByModule_mapsResults() {
        AuditLog entity = AuditLog.builder()
                .auditId(1L).employeeId("EMP007").userName("Anil Kumar")
                .action("LOGIN").module("AUTH").timestamp(LocalDateTime.now())
                .build();
        AuditLogResponse dto = AuditLogResponse.builder().auditId(1L).module("AUTH").build();

        when(auditLogRepository.findByModule("AUTH")).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(dto);

        List<AuditLogResponse> result = service.getLogsByModule("AUTH");

        assertThat(result).containsExactly(dto);
        verify(auditLogRepository).findByModule("AUTH");
    }
}
