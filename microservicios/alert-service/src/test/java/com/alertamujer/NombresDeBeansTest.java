package com.alertamujer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Spring nombra cada bean por su clase: dos adaptadores con el mismo nombre en módulos distintos
 * (p. ej. dos PushAdapter) impiden que el servicio arranque. Las pruebas unitarias no levantan
 * Spring, así que esto lo detecta sin base de datos.
 */
class NombresDeBeansTest {

    private static final Pattern BEAN = Pattern.compile("^@(?:[\\w.]+\\.)?(Component|Service|Repository|RestController|Controller|Configuration)\\b",
            Pattern.MULTILINE);

    @Test
    void noHayDosBeansConElMismoNombre() throws IOException {
        List<Path> fuentes = Stream.of(Path.of("src/main/java"), Path.of("../shared-kernel/src/main/java"))
                .flatMap(NombresDeBeansTest::archivos)
                .filter(NombresDeBeansTest::esBean)
                .toList();
        Map<String, List<Path>> porNombre = fuentes.stream()
                .collect(Collectors.groupingBy(p -> p.getFileName().toString(), Collectors.toList()));
        Map<String, List<Path>> repetidos = porNombre.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertEquals(Map.of(), repetidos);
    }

    private static Stream<Path> archivos(Path raiz) {
        try {
            return Files.walk(raiz).filter(p -> p.toString().endsWith(".java")).toList().stream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean esBean(Path archivo) {
        try {
            return BEAN.matcher(Files.readString(archivo)).find();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
