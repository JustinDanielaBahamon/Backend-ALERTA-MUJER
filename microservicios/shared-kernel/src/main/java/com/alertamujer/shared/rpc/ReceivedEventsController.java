package com.alertamujer.shared.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Recibe eventos de otros microservicios y los publica dentro de este, para que sus listeners
 * (@EventListener / @TransactionalEventListener con fallbackExecution) reaccionen igual que en el
 * monolito. Solo acepta los tipos declarados en EventosAceptados.
 */
@RestController
@RequestMapping("/internal/events")
public class ReceivedEventsController {

    /** Eventos que este servicio acepta de otros. */
    public record AcceptedEvents(List<Class<?>> types) {}

    private final Map<String, Class<?>> types;
    private final ObjectMapper json;
    private final ApplicationEventPublisher events;

    public ReceivedEventsController(ObjectProvider<AcceptedEvents> accepted, ObjectMapper json,
                                      ApplicationEventPublisher events) {
        this.types = accepted.getIfAvailable(() -> new AcceptedEvents(List.of())).types().stream()
                .collect(Collectors.toMap(Class::getSimpleName, Function.identity()));
        this.json = json;
        this.events = events;
    }

    @PostMapping("/{type}")
    public ResponseEntity<Void> receive(@PathVariable String type, @RequestBody JsonNode body) throws Exception {
        Class<?> eventClass = types.get(type);
        if (eventClass == null) return ResponseEntity.notFound().build();
        events.publishEvent(json.treeToValue(body, eventClass));
        return ResponseEntity.accepted().build();
    }
}
