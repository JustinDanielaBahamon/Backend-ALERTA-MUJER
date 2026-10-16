package com.alertamujer;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Fronteras del monolito modular (paso previo a microservicios): un módulo solo puede usar la
 * API pública de otro (paquetes *.api y el núcleo shared), nunca sus entidades, repositorios ni
 * servicios internos, y no puede haber dependencias circulares entre módulos.
 * Si un cambio rompe esa regla, esta prueba falla y el build no pasa.
 */
class ModularityTest {

    static final ApplicationModules MODULOS = ApplicationModules.of(AlertaMujerBackendApplication.class);

    @Test
    void losModulosSoloSeUsanPorSuApiPublica() {
        MODULOS.verify();
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
