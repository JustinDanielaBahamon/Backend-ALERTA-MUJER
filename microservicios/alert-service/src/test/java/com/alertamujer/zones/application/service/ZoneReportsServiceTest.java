package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.out.IdentityPort;
import com.alertamujer.zones.application.port.out.ZoneReportRepository;
import com.alertamujer.zones.application.port.out.ZoneRepository;
import com.alertamujer.zones.domain.model.Zone;
import com.alertamujer.zones.domain.model.ZoneReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** The report author is the token's profile (it used to be compared with users.id, which does not match). */
class ZoneReportsServiceTest {

    private final ZoneReportRepository repository = mock(ZoneReportRepository.class);
    private final ZoneRepository zones = mock(ZoneRepository.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final ZoneReportsService service = new ZoneReportsService(repository, zones, identity);

    @BeforeEach
    void setUp() {
        when(identity.requiredProfile(1L)).thenReturn(10L);
        when(identity.profileOf(1L)).thenReturn(Optional.of(10L));
        when(identity.requiredProfile(2L)).thenReturn(20L);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createUsesTheTokenProfileAndRequiresAnExistingZone() {
        when(zones.find(5L)).thenReturn(Optional.of(new Zone()));
        when(zones.find(9L)).thenReturn(Optional.empty());

        assertEquals(10L, service.create(5L, "insegura", null, 1L).getUserProfileId());
        assertThrows(ResourceNotFoundException.class, () -> service.create(9L, "insegura", null, 1L));
        assertThrows(ResourceNotFoundException.class, () -> service.create(null, "insegura", null, 1L));
    }

    @Test
    void onlyTheAuthorSeesOrDeletesHerReport() {
        ZoneReport own = ZoneReport.created(10L, 5L, "insegura", null);
        when(repository.find(3L)).thenReturn(Optional.of(own));

        assertTrue(service.one(3L, 1L).isPresent());
        assertThrows(AccessDeniedException.class, () -> service.delete(3L, 2L));
        service.delete(3L, 1L);
        verify(repository).delete(3L);
    }

    @Test
    void anotherUsersReportsAreOnlySeenByTheAdministration() {
        when(repository.ofProfile(10L)).thenReturn(List.of());
        when(repository.ofProfile(30L)).thenReturn(List.of());
        when(identity.profileOf(30L)).thenReturn(Optional.empty());

        assertEquals(List.of(), service.ofUser(1L, 1L, false));
        assertEquals(List.of(), service.ofUser(10L, 1L, false));
        assertThrows(AccessDeniedException.class, () -> service.ofUser(30L, 1L, false));
        assertEquals(List.of(), service.ofUser(30L, 1L, true));
    }

    @Test
    void approvingOrRejectingAMissingReportFails() {
        when(repository.find(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.approve(99L, 1));
        assertThrows(ResourceNotFoundException.class, () -> service.reject(99L, 1));
    }
}
