package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NewAlert;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertChanges;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Alert use cases with mocked ports (no database or other services). */
@ExtendWith(MockitoExtension.class)
class AlertsServiceTest {

    @Mock private AlertRepository repository;
    @Mock private RecipientsService recipients;
    @Mock private IdentityPort identity;
    @Mock private AlertEventsPort events;

    @InjectMocks private AlertsService service;

    private Alert alert;

    @BeforeEach
    void setUp() {
        alert = new Alert();
        alert.setId(1L);
        alert.setUserProfileId(10L);
        alert.setDeviceId(1L);
        alert.setActivationMethod("panic_button");
        alert.setStartedAt(LocalDateTime.now());
    }

    private void profile(Long userId, Long profile) {
        when(identity.profileOf(userId)).thenReturn(Optional.of(profile));
    }

    private static NewAlert newOne(List<Long> contactIds) {
        return new NewAlert(1L, null, "panic_button", null, null, BigDecimal.valueOf(4.6), BigDecimal.valueOf(-74.1),
                null, contactIds);
    }

    @Test
    void activate_theOwnerComesFromTheTokenAndContactsAreNotified() {
        profile(1L, 10L);
        when(repository.ongoing(10L)).thenReturn(Optional.empty());
        when(repository.save(any(Alert.class))).thenAnswer(inv -> {
            Alert a = inv.getArgument(0);
            a.setId(7L);
            return a;
        });

        Alert created = service.activate(newOne(List.of(3L)), 1L);

        assertEquals(10L, created.getUserProfileId());
        assertEquals("main", created.getAlertType());
        assertEquals("active", created.getStatus());
        assertEquals(BigDecimal.valueOf(4.6), created.getLatitude());
        verify(recipients).registrar(created, List.of(3L));
    }

    @Test
    void activate_withAnOngoingAlertReturnsItWithoutNotifyingAgain() {
        profile(1L, 10L);
        when(repository.ongoing(10L)).thenReturn(Optional.of(alert));

        assertSame(alert, service.activate(newOne(null), 1L));

        verify(repository, never()).save(any());
        verifyNoInteractions(recipients);
    }

    @Test
    void activate_withoutProfileFails() {
        when(identity.profileOf(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.activate(newOne(null), 1L));
        verifyNoInteractions(repository);
    }

    @Test
    void update_endingTheAlertNotifiesTheContacts() {
        profile(1L, 10L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));
        when(repository.save(alert)).thenReturn(alert);

        Alert result = service.update(1L, new AlertChanges(null, null, "resolved", null, null, null), 1L);

        assertEquals("resolved", result.getStatus());
        assertNotNull(result.getEndedAt());
        verify(events).alertEnded(eq(alert), any(LocalDateTime.class));
    }

    @Test
    void update_withoutStatusChangeDoesNotNotify() {
        profile(1L, 10L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));
        when(repository.save(alert)).thenReturn(alert);

        service.update(1L, new AlertChanges(null, null, null, "Voy en taxi", null, null), 1L);

        verifyNoInteractions(events);
    }

    @Test
    void updatingOrDeletingSomeoneElsesAlertIsRejected() {
        profile(2L, 20L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));

        assertThrows(AccessDeniedException.class,
                () -> service.update(1L, new AlertChanges(null, null, "cancelled", null, null, null), 2L));
        assertThrows(AccessDeniedException.class, () -> service.delete(1L, 2L));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_theOwnerDeletesHerAlert() {
        profile(1L, 10L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));

        service.delete(1L, 1L);

        verify(repository).delete(1L);
    }

    @Test
    void one_onlyReturnsOwnOnes() {
        profile(1L, 10L);
        profile(2L, 20L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));

        assertTrue(service.one(1L, 1L).isPresent());
        assertTrue(service.one(1L, 2L).isEmpty());
    }

    @Test
    void ofUser_acceptsTheUserIdOrTheOwnProfileId() {
        profile(1L, 10L);
        when(identity.user(1L)).thenReturn(Optional.of(new UserSummary(1L, "Ana", null, "ana@test.com", null, 1L, null)));
        when(repository.ofUser(10L)).thenReturn(List.of(alert));

        assertEquals(1, service.ofUser(1L, 1L).size());
        assertEquals(1, service.ofUser(10L, 1L).size());
    }

    @Test
    void ofUser_anotherUserIsOnlySeenByTheAdministration() {
        profile(1L, 10L);
        when(identity.user(1L)).thenReturn(Optional.of(new UserSummary(1L, "Ana", null, "ana@test.com", null, 1L, null)));

        assertThrows(AccessDeniedException.class, () -> service.ofUser(5L, 1L));

        profile(2L, 30L);
        profile(5L, 50L);
        when(identity.user(2L)).thenReturn(Optional.of(new UserSummary(2L, "Admin", null, "admin@test.com", null, UserSummary.ADMIN_ROLE, null)));
        when(repository.ofUser(50L)).thenReturn(List.of(alert));

        assertEquals(List.of(alert), service.ofUser(5L, 2L));
    }

    @Test
    void keepEvidence_countsTheOnesAlreadyKept() {
        profile(1L, 10L);
        when(repository.find(1L)).thenReturn(Optional.of(alert));
        when(repository.kept(10L)).thenReturn((long) Alert.MAX_KEPT_ALERTS);

        assertThrows(IllegalArgumentException.class, () -> service.keepEvidence(1L, true, 1L));

        when(repository.kept(10L)).thenReturn(4L);
        when(repository.save(alert)).thenReturn(alert);
        assertTrue(service.keepEvidence(1L, true, 1L).isKeepEvidence());
    }

    @Test
    void queries_forOtherModules() {
        Alert other = new Alert();
        other.setId(2L);
        other.setUserProfileId(10L);
        other.setKeepEvidence(true);
        Alert foreign = new Alert();
        foreign.setId(3L);
        foreign.setUserProfileId(20L);
        when(repository.all()).thenReturn(List.of(alert, other, foreign));
        when(repository.findMany(Set.of(1L, 2L))).thenReturn(List.of(alert, other));

        assertEquals(2L, service.countByProfile().get(10L));
        assertEquals(Set.of(2L), service.keepEvidence(Set.of(1L, 2L)));
        assertEquals(Set.of(), service.keepEvidence(Set.of()));
        assertTrue(service.find(null).isEmpty());
    }

    @Test
    void mine_newestToOldest() {
        profile(1L, 10L);
        Alert old = new Alert();
        old.setStartedAt(LocalDateTime.now().minusDays(1));
        when(repository.ofUser(10L)).thenReturn(List.of(old, alert));

        assertEquals(List.of(alert, old), service.mine(1L));
    }

    @Test
    void activate_doesNotTrustTheCreationDateIfMissing() {
        profile(1L, 10L);
        when(repository.ongoing(10L)).thenReturn(Optional.empty());
        when(repository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        service.activate(newOne(null), 1L);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(repository).save(captor.capture());
        assertNotNull(captor.getValue().getStartedAt());
        assertNull(captor.getValue().getId());
    }
}
