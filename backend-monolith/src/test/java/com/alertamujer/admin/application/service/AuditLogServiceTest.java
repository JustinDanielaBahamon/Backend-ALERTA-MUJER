package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.infrastructure.repository.AuditLogRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogService service;

    private AuditLog registro(Long id, Long cuenta) {
        AuditLog r = new AuditLog();
        r.setId(id);
        r.setAccountId(cuenta);
        r.setAction("UPDATE");
        r.setEntityType("ALERT");
        return r;
    }

    @Test
    void listar_agregaElNombreDeQuienActuo() {
        when(auditLogRepository.findByEntityType(eq("ALERT"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(registro(2L, 39L), registro(1L, null))));
        Account cuenta = new Account();
        cuenta.setId(39L);
        cuenta.setUserId(7L);
        when(accountRepository.findAllById(anyCollection())).thenReturn(List.of(cuenta));
        User admin = new User();
        admin.setId(7L);
        admin.setFirstName("Admin");
        admin.setLastName("Auditoria");
        admin.setEmail("admin@test.local");
        when(userRepository.findAllById(anyCollection())).thenReturn(List.of(admin));

        List<AuditLogService.AuditLogView> vista = service.listar("alert", 9999);

        assertEquals("Admin Auditoria", vista.get(0).actorName());
        assertNull(vista.get(1).actorName()); // cambio del sistema
        ArgumentCaptor<Pageable> pagina = ArgumentCaptor.forClass(Pageable.class);
        verify(auditLogRepository).findByEntityType(eq("ALERT"), pagina.capture());
        assertEquals(AuditLogService.LIMITE_MAXIMO, pagina.getValue().getPageSize());
    }
}
