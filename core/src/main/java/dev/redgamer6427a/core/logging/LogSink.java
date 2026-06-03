package dev.redgamer6427a.core.logging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.regex.Pattern;

public class LogSink {

    private static final Pattern ANSI_PATTERN = Pattern.compile("\033\\[[;\\d]*m");
    private static final Gson GSON = new GsonBuilder().create();

    @Getter
    @Setter
    private Path directory;

    private final boolean saveRaw;
    private final boolean saveJson;

    private BufferedWriter rawWriter;
    private BufferedWriter jsonWriter;

    private final String sessionTimestamp;

    @Builder
    public LogSink(Path directory, boolean saveRaw, boolean saveJson) {
        this.directory = directory;
        this.saveRaw = saveRaw;
        this.saveJson = saveJson;
        this.sessionTimestamp = String.valueOf(Instant.now().toEpochMilli());
        init();
    }

    private void init() {
        try {
            Files.createDirectories(directory);
            if (saveRaw) {
                Path rawPath = directory.resolve("latest.log");
                rawWriter = new BufferedWriter(new FileWriter(rawPath.toFile(), false));
            }
            if (saveJson) {
                Path jsonPath = directory.resolve("latest.json");
                jsonWriter = new BufferedWriter(new FileWriter(jsonPath.toFile(), false));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize LogSink", e);
        }
    }

    public void write(Level level, String thread, String origin, String renderedAnsi, String rawMessage) {
        if (saveRaw) writeRaw(renderedAnsi);
        if (saveJson) writeJson(level, thread, origin, rawMessage);
    }

    private void writeRaw(String ansiOutput) {
        String stripped = ANSI_PATTERN.matcher(ansiOutput).replaceAll("");
        try {
            rawWriter.write(stripped);
            rawWriter.newLine();
            rawWriter.flush();
        } catch (IOException e) {
            System.err.println("[LogSink] Failed to write raw log: " + e.getMessage());
        }
    }

    private void writeJson(Level level, String thread, String origin, String message) {
        LogEntry entry = new LogEntry(
                Instant.now().toEpochMilli(),
                level.name(),
                thread,
                origin,
                message
        );
        try {
            jsonWriter.write(GSON.toJson(entry));
            jsonWriter.newLine();
            jsonWriter.flush();
        } catch (IOException e) {
            System.err.println("[LogSink] Failed to write json log: " + e.getMessage());
        }
    }

    public void archiveCurrent() {
        close();
        try {
            if (saveRaw) {
                Path latest = directory.resolve("latest.log");
                Path archived = directory.resolve(sessionTimestamp + ".log");
                if (Files.exists(latest)) Files.copy(latest, archived);
            }
            if (saveJson) {
                Path latest = directory.resolve("latest.json");
                Path archived = directory.resolve(sessionTimestamp + ".json");
                if (Files.exists(latest)) Files.copy(latest, archived);
            }
        } catch (IOException e) {
            System.err.println("[LogSink] Failed to archive logs: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (rawWriter != null) rawWriter.close();
            if (jsonWriter != null) jsonWriter.close();
        } catch (IOException e) {
            System.err.println("[LogSink] Failed to close writers: " + e.getMessage());
        }
    }

    private record LogEntry(long timestamp, String level, String thread, String origin, String message) {}
}