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
class BeanNamesTest {

    private static final Pattern BEAN = Pattern.compile("^@(?:[\\w.]+\\.)?(Component|Service|Repository|RestController|Controller|Configuration)\\b",
            Pattern.MULTILINE);

    @Test
    void noTwoBeansShareAName() throws IOException {
        List<Path> fuentes = Stream.of(Path.of("src/main/java"), Path.of("../shared-kernel/src/main/java"))
                .flatMap(BeanNamesTest::files)
                .filter(BeanNamesTest::esBean)
                .toList();
        Map<String, List<Path>> byName = fuentes.stream()
                .collect(Collectors.groupingBy(p -> p.getFileName().toString(), Collectors.toList()));
        Map<String, List<Path>> repeated = byName.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertEquals(Map.of(), repeated);
    }

    private static Stream<Path> files(Path root) {
        try {
            return Files.walk(root).filter(p -> p.toString().endsWith(".java")).toList().stream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean esBean(Path file) {
        try {
            return BEAN.matcher(Files.readString(file)).find();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
