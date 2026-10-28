package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NewRoute;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.RouteRepository;
import com.alertamujer.alerts.domain.model.RoutePoint;
import com.alertamujer.alerts.domain.model.SavedRoute;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutesServiceTest {

    @Mock private RouteRepository repository;
    @Mock private IdentityPort identity;

    @InjectMocks private RoutesService service;

    private static final List<RoutePoint> POINTS = List.of(new RoutePoint(4.6, -74.1, null), new RoutePoint(4.7, -74.0, null));

    @BeforeEach
    void setUp() {
        lenient().when(identity.requiredProfile(1L)).thenReturn(10L);
    }

    private static NewRoute created() {
        return new NewRoute(" Casa ", "A", "B", "Bogotá", null, null, null, null, POINTS, null);
    }

    @Test
    void create_isStarredWhileThereIsRoom() {
        when(repository.count(10L)).thenReturn(1L);
        when(repository.countStarred(10L)).thenReturn(1L);
        when(repository.save(any(SavedRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        SavedRoute r = service.create(created(), 1L);

        assertEquals("Casa", r.getName());
        assertEquals(10L, r.getUserProfileId());
        assertTrue(r.getIsStarred());
        assertTrue(r.getIsManual());
    }

    @Test
    void create_withAll4TakenIsRejected() {
        when(repository.count(10L)).thenReturn(4L);

        assertThrows(ConflictException.class, () -> service.create(created(), 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void someoneElsesRouteCannotBeTouched() {
        SavedRoute foreign = new SavedRoute();
        foreign.setId(3L);
        foreign.setUserProfileId(20L);
        when(repository.find(3L)).thenReturn(Optional.of(foreign));

        assertThrows(AccessDeniedException.class, () -> service.update(3L, "Otro", null, 1L));
        assertThrows(AccessDeniedException.class, () -> service.delete(3L, 1L));
        verify(repository, never()).delete(any());
    }
}
