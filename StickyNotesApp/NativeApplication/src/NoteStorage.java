import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class NoteStorage {
    private final Path dir;

    NoteStorage() {
        dir = Paths.get(System.getProperty("user.home"), ".quicknotes");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
    }

    List<NoteModel> loadAll() {
        List<NoteModel> notes = new ArrayList<>();
        try {
            Files.list(dir)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(p -> {
                    try {
                        NoteModel m = NoteModel.fromJson(new String(Files.readAllBytes(p), StandardCharsets.UTF_8));
                        if (m != null) notes.add(m);
                    } catch (IOException ignored) {}
                });
        } catch (IOException ignored) {}
        notes.sort((a, b) -> Long.compare(b.updatedAt, a.updatedAt));
        return notes;
    }

    void save(NoteModel note) {
        try {
            Files.write(dir.resolve(note.id + ".json"), note.toJson().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
    }

    void delete(String id) {
        try { Files.deleteIfExists(dir.resolve(id + ".json")); } catch (IOException ignored) {}
    }
}
