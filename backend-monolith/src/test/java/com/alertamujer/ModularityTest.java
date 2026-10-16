package com.alertamujer;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fronteras del monolito modular (paso previo a microservicios): un módulo solo puede usar
 * la API pública de otro, nunca sus entidades, repositorios ni servicios internos.
 *
 * Mientras se separan los módulos, el número de violaciones solo puede bajar: si un cambio
 * agrega un acoplamiento nuevo, esta prueba falla. Al llegar a 0 se reemplaza por verify().
 */
class ModularityTest {

    static final ApplicationModules MODULOS = ApplicationModules.of(AlertaMujerBackendApplication.class);

    /** Bajar este número en cada commit que separa un módulo. */
    static final int MAXIMO_VIOLACIONES = 0;

    @Test
    void noAumentanLasDependenciasEntreModulos() {
        List<String> violaciones = MODULOS.detectViolations().getMessages();
        System.out.println("Violaciones de módulos: " + violaciones.size() + " (máximo " + MAXIMO_VIOLACIONES + ")");
        assertTrue(violaciones.size() <= MAXIMO_VIOLACIONES,
                () -> "Hay " + violaciones.size() + " violaciones de módulos (máximo " + MAXIMO_VIOLACIONES
                        + "). Un módulo está usando por dentro a otro:\n" + String.join("\n", violaciones.subList(0, Math.min(5, violaciones.size()))));
    }

    /** Diagramas de módulos (PlantUML) en target/spring-modulith-docs. */
    @Test
    void documentarModulos() {
        new Documenter(MODULOS)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();
    }
}
