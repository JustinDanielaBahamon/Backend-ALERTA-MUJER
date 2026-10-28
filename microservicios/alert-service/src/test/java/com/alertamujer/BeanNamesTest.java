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
 * Spring names each bean after its class: two adapters with the same name in different modules
 * (e.g. two PushAdapter) stop the service from starting. Unit tests do not start
 * Spring, so this detects it without a database.
 */
class BeanNamesTest {

    private static final Pattern BEAN = Pattern.compile("^@(?:[\\w.]+\\.)?(Component|Service|Repository|RestController|Controller|Configuration)\\b",
            Pattern.MULTILINE);

    @Test
    void noTwoBeansShareAName() throws IOException {
        List<Path> sources = Stream.of(Path.of("src/main/java"), Path.of("../shared-kernel/src/main/java"))
                .flatMap(BeanNamesTest::files)
                .filter(BeanNamesTest::isBean)
                .toList();
        Map<String, List<Path>> byName = sources.stream()
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

    private static boolean isBean(Path file) {
        try {
            return BEAN.matcher(Files.readString(file)).find();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
