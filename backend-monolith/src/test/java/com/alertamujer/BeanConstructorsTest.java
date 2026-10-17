package com.alertamujer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Las pruebas no levantan el contexto completo (necesitaría la base). Esta revisa algo que sí lo
 * rompe al arrancar: un componente con varios constructores y ninguno marcado con @Autowired.
 */
class BeanConstructorsTest {

    @Test
    void cadaComponenteTieneUnConstructorClaro() throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
        List<String> ambiguos = new ArrayList<>();
        for (var bd : scanner.findCandidateComponents("com.alertamujer")) {
            Class<?> clase = Class.forName(bd.getBeanClassName());
            if (clase.isInterface() || Modifier.isAbstract(clase.getModifiers())) continue;
            Constructor<?>[] constructores = clase.getDeclaredConstructors();
            boolean conAutowired = Arrays.stream(constructores).anyMatch(c -> c.isAnnotationPresent(Autowired.class));
            boolean sinArgumentos = Arrays.stream(constructores).anyMatch(c -> c.getParameterCount() == 0);
            if (constructores.length > 1 && !conAutowired && !sinArgumentos) ambiguos.add(clase.getName());
        }
        assertTrue(ambiguos.isEmpty(), "Componentes sin un constructor claro para Spring: " + ambiguos);
    }
}
