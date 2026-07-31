package com.aluminibackend.api.module.audit.service;

import com.aluminibackend.api.integration.audit.AuditFeignClient;
import com.aluminibackend.api.integration.audit.RecordAuditRequest;
import com.aluminibackend.api.module.user.entity.User;
import com.aluminibackend.api.module.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Feign-adapter tests: the monolith resolves the user-name snapshot and delegates
 * to audit-service, swallowing downstream failures (best-effort).
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock private AuditFeignClient auditFeignClient;
    @Mock private UserRepository userRepository;

    @InjectMocks private AuditLogServiceImpl service;

    @Test
    void record_resolvesUserNameAndDelegates() {
        User user = User.builder().employeeId("EMP007").name("Anil Kumar").build();
        when(userRepository.findByEmployeeId("EMP007")).thenReturn(Optional.of(user));

        service.record("EMP007", "CREATE_REHIRE_ELIGIBILITY", "REHIRE_ELIGIBILITY");

        ArgumentCaptor<RecordAuditRequest> captor = ArgumentCaptor.forClass(RecordAuditRequest.class);
        verify(auditFeignClient).record(captor.capture());
        RecordAuditRequest sent = captor.getValue();
        assertThat(sent.getEmployeeId()).isEqualTo("EMP007");
        assertThat(sent.getUserName()).isEqualTo("Anil Kumar");
        assertThat(sent.getAction()).isEqualTo("CREATE_REHIRE_ELIGIBILITY");
        assertThat(sent.getModule()).isEqualTo("REHIRE_ELIGIBILITY");
    }

    @Test
    void record_nullUserId_sendsNullSnapshot() {
        service.record(null, "SYSTEM_EVENT", "AUTH");

        ArgumentCaptor<RecordAuditRequest> captor = ArgumentCaptor.forClass(RecordAuditRequest.class);
        verify(auditFeignClient).record(captor.capture());
        assertThat(captor.getValue().getEmployeeId()).isNull();
        assertThat(captor.getValue().getUserName()).isNull();
        verifyNoInteractions(userRepository);
    }

    @Test
    void record_downstreamFailure_isSwallowed() {
        when(userRepository.findByEmployeeId("EMP007"))
                .thenReturn(Optional.of(User.builder().employeeId("EMP007").name("X").build()));
        doThrow(new RuntimeException("audit-service down")).when(auditFeignClient).record(any());

        // Must not propagate — audit is a best-effort side effect.
        service.record("EMP007", "LOGIN", "AUTH");

        verify(auditFeignClient).record(any());
    }
}
