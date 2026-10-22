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

    private final ApplicationContext contexto;
    private final ObjectMapper json;
    private final Map<String, Class<?>> contratos;

    public RpcServerController(ApplicationContext contexto, ObjectMapper json, ObjectProvider<ContratosExpuestos> expuestos) {
        this.contexto = contexto;
        this.json = json;
        ContratosExpuestos c = expuestos.getIfAvailable(() -> new ContratosExpuestos(java.util.List.of()));
        this.contratos = c.contratos().stream().collect(Collectors.toMap(Class::getSimpleName, Function.identity()));
    }

    @PostMapping("/{contrato}/{metodo}")
    public ResponseEntity<Object> invocar(@PathVariable String contrato, @PathVariable String metodo,
                                          @RequestBody JsonNode argumentos) throws Exception {
        Class<?> type = contratos.get(contrato);
        if (type == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorRemoto("NoExpuesto", contrato));
        int cantidad = argumentos == null ? 0 : argumentos.size();
        Method m = Arrays.stream(type.getMethods())
                .filter(x -> x.getName().equals(metodo) && x.getParameterCount() == cantidad)
                .findFirst().orElse(null);
        if (m == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorRemoto("NoExiste", metodo));

        Object[] args = new Object[cantidad];
        for (int i = 0; i < cantidad; i++) {
            args[i] = json.readValue(json.treeAsTokens(argumentos.get(i)),
                    json.getTypeFactory().constructType(m.getGenericParameterTypes()[i]));
        }
        try {
            return ResponseEntity.ok(m.invoke(contexto.getBean(type), args));
        } catch (InvocationTargetException e) {
            Throwable causa = e.getCause();
            return ResponseEntity.status(estadoDe(causa)).body(new ErrorRemoto(causa.getClass().getSimpleName(), causa.getMessage()));
        }
    }

    private static HttpStatus estadoDe(Throwable e) {
        if (e instanceof ResourceNotFoundException) return HttpStatus.NOT_FOUND;
        if (e instanceof AccessDeniedException) return HttpStatus.FORBIDDEN;
        if (e instanceof IllegalArgumentException) return HttpStatus.BAD_REQUEST;
        if (e instanceof IllegalStateException) return HttpStatus.CONFLICT;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
