package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.SettingsPort;
import com.alertamujer.alerts.application.port.out.PhonesPort;
import com.alertamujer.alerts.application.port.out.ContactRepository;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.ContactData;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.shared.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyContactsServiceTest {

    @Mock private ContactRepository repository;
    @Mock private IdentityPort identity;
    @Mock private SettingsPort settings;
    @Mock private PhonesPort phones;

    @InjectMocks private EmergencyContactsService service;

    private static final ContactData EVA = new ContactData("Eva", "300", "eva@correo.com", "Hermana", null);

    @BeforeEach
    void setUp() {
        lenient().when(identity.profileOf(1L)).thenReturn(Optional.of(10L));
    }

    @Test
    void create_allowsUpToTheLimitAndTheOwnerComesFromTheToken() {
        when(settings.maxContacts()).thenReturn(5);
        when(repository.ofUser(10L)).thenReturn(Collections.nCopies(4, new EmergencyContact()));
        when(repository.save(any(EmergencyContact.class))).thenAnswer(inv -> inv.getArgument(0));

        EmergencyContact created = service.create(EVA, 1L);

        assertEquals(10L, created.getUserProfileId());
        assertEquals("Eva", created.getContactName());
    }

    @Test
    void appStatus_distinguishesWithAppWithoutPhoneAndWithoutAccount() {
        when(repository.ofUser(10L)).thenReturn(List.of(
                contact(1L, "eva@correo.com"), contact(2L, "ana@correo.com"), contact(3L, "nadie@correo.com"), contact(4L, null)));
        when(identity.userByEmail("eva@correo.com")).thenReturn(Optional.of(50L));
        when(identity.userByEmail("ana@correo.com")).thenReturn(Optional.of(60L));
        when(identity.userByEmail("nadie@correo.com")).thenReturn(Optional.empty());
        when(phones.phoneTokens(50L)).thenReturn(List.of("token"));
        when(phones.phoneTokens(60L)).thenReturn(List.of());

        assertEquals(Map.of(1L, "app", 2L, "account", 3L, "none", 4L, "none"), service.appStatus(1L));
    }

    private static EmergencyContact contact(Long id, String email) {
        EmergencyContact c = EmergencyContact.created(10L, new ContactData("X", "300", email, null, null));
        c.setId(id);
        return c;
    }

    @Test
    void create_rejectsWhenOverTheLimit() {
        when(settings.maxContacts()).thenReturn(5);
        when(repository.ofUser(10L)).thenReturn(Collections.nCopies(5, new EmergencyContact()));

        assertThrows(IllegalArgumentException.class, () -> service.create(EVA, 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void create_rejectsAPhotoThatIsNotAnImage() {
        when(settings.maxContacts()).thenReturn(5);
        when(repository.ofUser(10L)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.create(new ContactData("Eva", null, null, null, "<script>"), 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void updatingOrDeletingSomeoneElsesContactIsRejected() {
        EmergencyContact foreign = EmergencyContact.created(20L, EVA);
        foreign.setId(3L);
        when(repository.find(3L)).thenReturn(Optional.of(foreign));

        assertThrows(AccessDeniedException.class, () -> service.update(3L, EVA, 1L));
        assertThrows(AccessDeniedException.class, () -> service.delete(3L, 1L));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test
    void ofUser_anotherUsersOnesAreSeenByNobody() {
        // Ni siquiera la administración: son datos de terceros (auditoría S2)
        assertThrows(AccessDeniedException.class, () -> service.ofUser(5L, 1L));
        verify(repository, never()).ofUser(5L);
    }

    @Test
    void one_onlyReturnsOwnOnes() {
        EmergencyContact own = EmergencyContact.created(10L, EVA);
        EmergencyContact foreign = EmergencyContact.created(20L, EVA);
        when(repository.find(1L)).thenReturn(Optional.of(own));
        when(repository.find(2L)).thenReturn(Optional.of(foreign));

        assertTrue(service.one(1L, 1L).isPresent());
        assertTrue(service.one(2L, 1L).isEmpty());
    }
}
