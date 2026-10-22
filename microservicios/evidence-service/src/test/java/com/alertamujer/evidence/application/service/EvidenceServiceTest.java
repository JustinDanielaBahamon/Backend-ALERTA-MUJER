package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.out.AlertsPort;
import com.alertamujer.evidence.application.port.out.EvidenceStorage;
import com.alertamujer.evidence.application.port.out.EvidenceStorage.IncomingFile;
import com.alertamujer.evidence.application.port.out.EvidenceStorage.StoredFile;
import com.alertamujer.evidence.application.port.out.EvidenceRepository;
import com.alertamujer.evidence.application.port.out.IdentityPort;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.shared.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** User use cases, tested against fake ports (no real database or storage). */
class EvidenceServiceTest {

    private final EvidenceRepository repository = mock(EvidenceRepository.class);
    private final EvidenceStorage storage = mock(EvidenceStorage.class);
    private final AlertsPort alerts = mock(AlertsPort.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final EvidenceService service = new EvidenceService(repository, storage, alerts, identity, 1000);

    private Evidence evidence;

    @BeforeEach
    void setUp() {
        evidence = new Evidence();
        evidence.setId(4L);
        evidence.setAlertId(9L);
        evidence.setMediaType("video");
        evidence.setFileName("video_1760870000000.mp4");
        when(repository.find(4L)).thenReturn(Optional.of(evidence));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(alerts.find(9L)).thenReturn(Optional.of(
                new AlertSummary(9L, 70L, "main", "panic_button", "active", null, null, null, null, null, null)));
        when(identity.profileOf(1L)).thenReturn(70L);
        when(identity.profileOf(2L)).thenReturn(71L);
    }

    private IncomingFile file(String type, long size) {
        return new IncomingFile("foto.jpg", type, size, () -> new ByteArrayInputStream(new byte[(int) size]));
    }

    @Test
    void renameSavesTheNewName() {
        assertEquals("Camino a casa.mp4", service.rename(4L, "Camino a casa", 1L).evidence().getFileName());
    }

    @Test
    void anotherUserCannotViewOrRename() {
        assertThrows(AccessDeniedException.class, () -> service.una(4L, 2L));
        assertThrows(AccessDeniedException.class, () -> service.rename(4L, "x", 2L));
        verify(repository, never()).save(any());
    }

    @Test
    void uploadSavesTheFileAndItsHash() {
        when(storage.save(any())).thenReturn(new StoredFile("clave", "abc123", 10));

        Evidence saved = service.upload(file("image/jpeg", 10), 9L, 1L).evidence();

        assertEquals("photo", saved.getMediaType());
        assertEquals("abc123", saved.getSha256());
        assertTrue(saved.hasFile());
    }

    @Test
    void ifRegistrationFailsTheUploadedFileIsDeleted() {
        when(storage.save(any())).thenReturn(new StoredFile("clave", "abc123", 10));
        when(repository.save(any())).thenThrow(new IllegalStateException("base caída"));

        assertThrows(IllegalStateException.class, () -> service.upload(file("image/jpeg", 10), 9L, 1L));
        verify(storage).delete("clave");
    }

    @Test
    void rejectsTooLargeFilesBeforeSavingThem() {
        assertThrows(IllegalArgumentException.class, () -> service.upload(file("video/mp4", 5000), 9L, 1L));
        verifyNoInteractions(storage);
    }

    @Test
    void listsTheEvidenceOfTheUsersAlerts() {
        when(alerts.idsOfUser(70L)).thenReturn(List.of(9L));
        when(repository.ofAlerts(List.of(9L))).thenReturn(List.of(evidence));
        when(alerts.findMany(any())).thenReturn(Map.of());

        assertEquals(1, service.ofUser(1L).size());
    }
}
