package com.alertamujer.shared.rpc;

import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adaptador de entrada para los otros microservicios: POST /internal/rpc/{Contrato}/{metodo} con los
 * argumentos en un arreglo JSON ejecuta el método de la implementación local de ese contrato.
 * Es el lado servidor del cliente genérico (RpcClientFactory), al estilo de Feign.
 */
@RestController
@RequestMapping("/internal/rpc")
public class RpcServerController {

    private final ApplicationContext context;
    private final ObjectMapper json;
    private final Map<String, Class<?>> contracts;

    public RpcServerController(ApplicationContext context, ObjectMapper json, ObjectProvider<ExposedContracts> exposed) {
        this.context = context;
        this.json = json;
        ExposedContracts c = exposed.getIfAvailable(() -> new ExposedContracts(java.util.List.of()));
        this.contracts = c.contracts().stream().collect(Collectors.toMap(Class::getSimpleName, Function.identity()));
    }

    @PostMapping("/{contract}/{method}")
    public ResponseEntity<Object> invoke(@PathVariable String contract, @PathVariable String method,
                                          @RequestBody JsonNode arguments) throws Exception {
        Class<?> type = contracts.get(contract);
        if (type == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RemoteError("NoExpuesto", contract));
        int count = arguments == null ? 0 : arguments.size();
        Method m = Arrays.stream(type.getMethods())
                .filter(x -> x.getName().equals(method) && x.getParameterCount() == count)
                .findFirst().orElse(null);
        if (m == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RemoteError("NoExiste", method));

        Object[] args = new Object[count];
        for (int i = 0; i < count; i++) {
            args[i] = json.readValue(json.treeAsTokens(arguments.get(i)),
                    json.getTypeFactory().constructType(m.getGenericParameterTypes()[i]));
        }
        try {
            return ResponseEntity.ok(m.invoke(context.getBean(type), args));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            return ResponseEntity.status(statusOf(cause)).body(new RemoteError(cause.getClass().getSimpleName(), cause.getMessage()));
        }
    }

    private static HttpStatus statusOf(Throwable e) {
        if (e instanceof ResourceNotFoundException) return HttpStatus.NOT_FOUND;
        if (e instanceof AccessDeniedException) return HttpStatus.FORBIDDEN;
        if (e instanceof IllegalArgumentException) return HttpStatus.BAD_REQUEST;
        if (e instanceof IllegalStateException) return HttpStatus.CONFLICT;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
