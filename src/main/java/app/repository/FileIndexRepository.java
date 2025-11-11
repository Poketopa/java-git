package main.java.app.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import main.java.app.domain.Index;

public final class FileIndexRepository implements app.repository.IndexRepository {
    private static final String DOT_JGIT = ".jgit";
    private static final String INDEX = "index";
    private final Path rootDir;

    public FileIndexRepository(Path rootDir) {
        this.rootDir = Objects.requireNonNull(rootDir, "rootDir");
    }

    @Override
    public Index read() {
        Path indexFile = rootDir.resolve(DOT_JGIT).resolve(INDEX);
        if (!Files.exists(indexFile)) {
            return new Index(Map.of());
        }
        try {
            List<String> lines = Files.readAllLines(indexFile, StandardCharsets.UTF_8);
            Map<String, String> map = new LinkedHashMap<>();
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                int firstSpace = line.indexOf(' ');
                if (firstSpace <= 0) continue;
                String sha = line.substring(0, firstSpace);
                String path = line.substring(firstSpace + 1);
                map.put(path, sha);
            }
            return new Index(map);
        } catch (IOException e) {
            return new Index(Map.of());
        }
    }

    @Override
    public void write(Index index) {
        Path dot = rootDir.resolve(DOT_JGIT);
        Path indexFile = dot.resolve(INDEX);
        try {
            Files.createDirectories(dot);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> e : index.stagedFiles().entrySet()) {
                sb.append(e.getValue()).append(' ').append(e.getKey()).append('\n');
            }
            Files.writeString(indexFile, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}


