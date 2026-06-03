package dev.redgamer6427a.core.files;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.utils.IOThrowingBiFunction;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Represents a file on the filesystem with extended utilities.
 * Provides safe methods for reading, writing, renaming, moving, and querying files.
 * <p>
 * Factory methods {@link #of(File)}, {@link #of(Path)}, and {@link #of(String)} should be used
 * to create instances. Use {@link #create()} to physically create the file if needed.
 */
public class ExtFile {

    private static final Logger logger = Logger.create();
    /**
     * The underlying path this instance represents.
     */
    @Getter
    private Path path;

    /**
     * Protected constructor; use factory methods to create an instance.
     *
     * @param path The path this instance represents.
     */
    protected ExtFile(Path path) {
        this.path = path;
    }

    /**
     * Creates a new ExtFile instance from a {@link File}.
     * <p>Does not create the file on disk; use {@link #create()} to do so.</p>
     *
     * @param file The file object to reference.
     * @return A new ExtFile instance.
     */
    public static ExtFile of(File file) {
        return new ExtFile(file.toPath());
    }

    /**
     * Creates a new ExtFile instance from a {@link Path}.
     * <p>Does not create the file on disk; use {@link #create()} to do so.</p>
     *
     * @param path The path to reference.
     * @return A new ExtFile instance.
     */
    public static ExtFile of(Path path) {
        return new ExtFile(path);
    }

    /**
     * Creates a new ExtFile instance from a string path.
     * <p>Does not create the file on disk; use {@link #create()} to do so.</p>
     *
     * @param path The string path.
     * @return An Optional containing the new ExtFile if the path is valid, or empty if invalid.
     */
    public static Optional<ExtFile> of(String path) {
        try {
            return Optional.of(of(Paths.get(path)));
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }

    /**
     * Reads text from a text files
     * @return An optional empty if the file does not exist or if it isn't plaintext but the file contents otherwise.
     * @throws IOException if an IO exception occurs.
     */
    public Optional<String> readTextFile() throws IOException {

        if (!exists() || !isPlainText()) {
            return Optional.empty();
        }

        return Optional.of(Files.readString(path));
    }

    /**
     * Read GSON object data from a file
     * @param type the type to read.
     * @return An optional empty if the file does not exist or if it isn't plaintext but the file contents as an object otherwise.
     * @throws IOException if an IO exception occurs.
     */
    public <T> Optional<T> readGson(TypeToken<T> type) throws IOException {

        Optional<String> json = readTextFile();

        return json.map(s -> new GsonBuilder().disableHtmlEscaping().create().fromJson(s, type));

    }

    /**
     * Writes GSON to a file.
     * @param obj the object to gsonify and write.
     * @throws IOException if an IO exception occurs.
     */
    public void writeGson(Object obj) throws IOException {
        writeText(new GsonBuilder().disableHtmlEscaping().create().toJson(obj), WriteMode.OVERWRITE_ALL);
    }

    /**
     * Creates the file on disk if it does not already exist.
     *
     * @return true if the file was created; false if it already existed.
     * @throws IOException if an I/O error occurs.
     */
    public boolean create() throws IOException {
        if (exists()) return false;

        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        Files.createFile(path);
        return true;
    }

    /**
     * Creates the directory on disk if it does not already exist.
     *
     * @return true if the directory was created; false if it already existed.
     * @throws IOException if an I/O error occurs.
     */
    public boolean createDir() throws IOException {
        if (exists()) return false;

        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        Files.createDirectory(path);
        return true;
    }

    /**
     * Asynchronously downloads a file from a URL.
     *
     * @param url              The URL to download from.
     * @param progressCallback Callback for bytesRead/totalBytes. Can be null.
     * @return A CompletableFuture that completes when the download finishes.
     */
    public CompletableFuture<Void> downloadFrom(URL url, BiConsumer<Long, Long> progressCallback) {

        return CompletableFuture.runAsync(() -> {
            try {
                if (Files.exists(path)) throw new IOException("File already exists");

                Files.createDirectories(path.getParent());
                try (InputStream in = url.openStream(); OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {

                    long totalBytes = -1;
                    try {
                        totalBytes = url.openConnection().getContentLengthLong();
                    } catch (Exception ignored) {
                    }

                    byte[] buffer = new byte[8192];
                    int read;
                    long bytesRead = 0;

                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        bytesRead += read;
                        if (progressCallback != null) progressCallback.accept(bytesRead, totalBytes);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Asynchronously uploads a file using multipart/form-data.
     *
     * @param url              The target URL.
     * @param progressCallback Callback for bytesUploaded/totalBytes. Can be null.
     * @param fieldName        Form field name for the file (usually "file").
     * @return CompletableFuture that completes with the HTTP response code.
     */
    public CompletableFuture<Integer> uploadFile(URL url, BiConsumer<Long, Long> progressCallback, String fieldName) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.exists(path)) throw new IOException("File does not exist.");
                if (!isPlainText()) throw new IOException("Binary file uploads may not be supported.");

                String boundary = "----Boundary" + UUID.randomUUID();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                long totalBytes = Files.size(path);
                long bytesUploaded = 0;

                try (OutputStream out = conn.getOutputStream()) {
                    // Multipart header
                    String header = "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + path.getFileName() + "\"\r\n" + "Content-Type: application/octet-stream\r\n\r\n";
                    out.write(header.getBytes(StandardCharsets.UTF_8));

                    // Stream file
                    try (InputStream in = Files.newInputStream(path)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                            bytesUploaded += read;
                            if (progressCallback != null) progressCallback.accept(bytesUploaded, totalBytes);
                        }
                    }

                    // Multipart footer
                    String footer = "\r\n--" + boundary + "--\r\n";
                    out.write(footer.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }

                int responseCode = conn.getResponseCode();

                try (InputStream responseStream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()) {
                    if (responseStream != null) {
                        new ByteArrayOutputStream().write(responseStream.readAllBytes());
                    }
                }

                return responseCode;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Checks whether the file exists on disk.
     *
     * @return true if the file exists; false otherwise.
     */
    public boolean exists() {
        return Files.exists(path);
    }

    /**
     * Returns the file's extension.
     *
     * @return The extension, or empty if none.
     */
    public String getFileExtension() {
        String name = getFullName();

        if (!name.contains(".")) return "";

        int i = name.lastIndexOf('.');
        return (i > 0 && i < name.length() - 1) ? name.substring(i + 1) : "";
    }

    /**
     * Checks if the file is likely plaintext.
     * <p>This reads only the first 4 KB of the file to make a guess,
     * avoiding reading the entire file for large files.</p>
     *
     * @return true if the file appears to be plain text; false otherwise.
     */
    public boolean isPlainText() {
        if (!Files.isRegularFile(path)) return false;

        final int MAX_BYTES = 4096;

        try (FileInputStream in = new FileInputStream(path.toFile())) {
            byte[] buffer = new byte[MAX_BYTES];
            int bytesRead = in.read(buffer);

            if (bytesRead == -1) return true; // empty file

            int printable = 0;
            for (int i = 0; i < bytesRead; i++) {
                byte b = buffer[i];
                if ((b >= 32 && b < 127) || b == '\n' || b == '\r' || b == '\t') {
                    printable++;
                } else if (b == 0) {
                    return false; // null byte → binary
                }
            }
            return ((double) printable / bytesRead) > 0.95;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns direct children of a directory.
     *
     * @return An array of ExtFile representing direct children; empty if none or if not a directory.
     */
    public ExtFile[] directChildren() {
        try (Stream<Path> stream = Files.list(path)) {
            return stream.map(ExtFile::new).toArray(ExtFile[]::new);
        } catch (IOException e) {
            return new ExtFile[0];
        }
    }

    /**
     * Returns all children of a directory recursively.
     *
     * @return An array of ExtFile representing all children; empty if none or if not a directory.
     */
    public ExtFile[] allChildren() {
        try (Stream<Path> s = Files.walk(path)) {
            return s.skip(1).map(ExtFile::new).toArray(ExtFile[]::new);
        } catch (IOException e) {
            return new ExtFile[0];
        }
    }

    /**
     * Deletes this file or directory (recursively).
     *
     * @param IOExHandler Handler for I/O exceptions per file.
     * @return Array of deleted files (empty if single file or errors occurred).
     */
    @SuppressWarnings("UnusedReturnValue")
    public ExtFile[] delete(@Nullable BiConsumer<ExtFile, IOException> IOExHandler) {
        if (!exists()) return new ExtFile[0];

        if (isDir()) {
            List<ExtFile> list = new ArrayList<>();
            for (ExtFile file : directChildren()) {
                list.add(file);
                list.addAll(Arrays.asList(file.allChildren()));
                try {
                    Files.delete(file.path);
                } catch (IOException e) {
                    if (IOExHandler != null) IOExHandler.accept(file, e);
                }
            }
            try {
                Files.delete(path);
            } catch (IOException e) {
                if (IOExHandler != null) IOExHandler.accept(this, e);
            }
            return list.toArray(ExtFile[]::new);
        } else {
            try {
                Files.delete(path);
            } catch (IOException e) {
                if (IOExHandler != null) {
                    IOExHandler.accept(this, e);
                }
            }
            return new ExtFile[0];
        }
    }

    /**
     * Checks if this file is a directory.
     *
     * @return true if a directory; false otherwise.
     */
    public boolean isDir() {
        return Files.isDirectory(path);
    }

    @Override
    public String toString() {
        try {
            return "ExtFile[path=\"" + path.toRealPath() + "\"]";
        } catch (IOException e) {
            return "ExtFile[path=\"" + path.toAbsolutePath() + "\"]";
        }
    }

    /**
     * Renames the file (name and extension)
     *
     * @param newName     New name.
     * @param copyOptions Copy options.
     * @throws IOException If an I/O error occurs.
     */
    public void setFullName(String newName, StandardCopyOption... copyOptions) throws IOException {
        path = Files.move(path, path.resolveSibling(newName), copyOptions);
    }

    /**
     * Renames the file (keeps extension).
     *
     * @param newName     New name without extension.
     * @param copyOptions Copy options.
     * @throws IOException If an I/O error occurs.
     */
    public void setName(String newName, StandardCopyOption... copyOptions) throws IOException {
        if (getFileExtension().isEmpty()) {
            path = Files.move(path, path.resolveSibling(newName), copyOptions);
        } else {
            path = Files.move(path, path.resolveSibling(newName + "." + getFileExtension()), copyOptions);
        }
    }

    /**
     * Sets a new extension.
     *
     * @param newExtension New extension.
     * @param copyOptions  Copy options.
     * @throws IOException If an I/O error occurs.
     */
    public void setExtension(String newExtension, StandardCopyOption... copyOptions) throws IOException {
        if (newExtension.isEmpty()) {
            path = Files.move(path, path.resolveSibling(getName()), copyOptions);
        } else {
            path = Files.move(path, path.resolveSibling(getName() + "." + newExtension), copyOptions);
        }
    }

    /**
     * Moves the file into another directory.
     *
     * @param newPath     Target directory.
     * @param copyOptions Copy options.
     * @throws IOException If an I/O error occurs.
     */
    public void setParent(Path newPath, StandardCopyOption... copyOptions) throws IOException {
        path = Files.move(path, newPath.resolve(getFullName()), copyOptions);
    }

    /**
     * Sets this object's path. Moves the file into the new path if it exists.
     *
     * @param newPath     New file path.
     * @param copyOptions Copy options.
     * @throws IOException If an I/O error occurs.
     */
    public void setPath(Path newPath, StandardCopyOption... copyOptions) throws IOException {

        if (exists()) {
            path = newPath;
        } else {
            path = Files.move(path, newPath, copyOptions);
        }


    }

    /**
     * Copies this file to a new location.
     *
     * @param toPath      Target path.
     * @param copyOptions Copy options.
     * @return New ExtFile instance for the copied file.
     * @throws IOException If an I/O error occurs.
     */
    public ExtFile copy(Path toPath, StandardCopyOption... copyOptions) throws IOException {
        Path other = Files.copy(path, toPath, copyOptions);
        return ExtFile.of(other);
    }

    /**
     * Writes text into a plaintext file.
     *
     * @param s         Content to write.
     * @param writeMode Write mode.
     * @throws IOException If the file does not exist or is not plaintext.
     * @see #create()
     */
    public void writeText(String s, WriteMode writeMode) throws IOException {
        if (!exists()) throw new IOException("Cannot write to file: use {@link #create()} first.");
        if (!isPlainText()) throw new IOException("Cannot write text to binary file.");

        if (writeMode == WriteMode.OVERWRITE_ALL) {
            new FileOutputStream(path.toFile()).close();
        }


        Files.writeString(path, s, writeMode == WriteMode.APPEND ? StandardOpenOption.APPEND : StandardOpenOption.WRITE);
    }

    /**
     * Writes text at a specific line and column in a plaintext file.
     *
     * @param s          Content to write.
     * @param append     Whether to append at the column or overwrite.
     * @param lineNumber 1-based line number.
     * @param column     1-based column.
     * @throws IOException If the file does not exist or is not plaintext.
     * @see #create()
     */
    public void writeText(String s, boolean append, int lineNumber, int column) throws IOException {
        if (!exists()) throw new IOException("Cannot write to file: use create() first.");
        if (!isPlainText()) throw new IOException("Cannot write text to binary file.");

        List<String> lines = Files.readAllLines(path);
        int lineIndex = lineNumber - 1;

        while (lines.size() <= lineIndex) lines.add("");
        String line = lines.get(lineIndex);
        int col = Math.min(column - 1, line.length());

        String newLine = append ? line.substring(0, col) + s + line.substring(col) : line.substring(0, col) + s + ((col + s.length() < line.length()) ? line.substring(col + s.length()) : "");
        lines.set(lineIndex, newLine);
        Files.write(path, lines);
    }

    /**
     * Replaces text in a file between two positions.
     *
     * @param replacement Replacement text.
     * @param fromLine    Start line (1-based).
     * @param fromCol     Start column (1-based).
     * @param toLine      End line (1-based).
     * @param toCol       End column (1-based).
     * @throws IOException If the file does not exist or is not plaintext.
     */
    public void replaceRange(String replacement, int fromLine, int fromCol, int toLine, int toCol) throws IOException {
        if (!exists()) throw new IOException("Cannot write to file: use {@link #create()} first.");
        if (!isPlainText()) throw new IOException("Cannot modify binary file.");

        List<String> lines = Files.readAllLines(path);

        int startLine = fromLine - 1;
        int endLine = toLine - 1;
        int startCol = fromCol - 1;
        int endCol = toCol - 1;

        while (lines.size() <= endLine) lines.add("");
        startCol = Math.min(startCol, lines.get(startLine).length());
        endCol = Math.min(endCol, lines.get(endLine).length());

        if (startLine == endLine) {
            String line = lines.get(startLine);
            lines.set(startLine, line.substring(0, startCol) + replacement + line.substring(endCol));
        } else {
            String firstLine = lines.get(startLine).substring(0, startCol) + replacement;
            String lastLine = lines.get(endLine).substring(endCol);
            if (endLine >= startLine + 1) lines.subList(startLine + 1, endLine + 1).clear();
            lines.set(startLine, firstLine + lastLine);
        }

        Files.write(path, lines);
    }

    /**
     * Returns the file name without the extension.
     *
     * @return File name without extension.
     */
    public String getName() {
        String full = getFullName();

        if (!full.contains(".")) {
            return full;
        }

        int dotIndex = full.lastIndexOf('.');
        return dotIndex > 0 ? full.substring(0, dotIndex) : full;
    }

    /**
     * Writes bytes into a file.
     *
     * @param bytes     Content to write.
     * @param writeMode Write mode.
     * @throws IOException If the file does not exist.
     * @see #create()
     */
    public void writeBytes(byte[] bytes, WriteMode writeMode) throws IOException {
        if (!exists()) throw new IOException("Cannot write to file: use {@link #create()} first.");

        if (writeMode == WriteMode.OVERWRITE_ALL) {
            new FileOutputStream(path.toFile()).close();
        }

        Files.write(path, bytes, writeMode == WriteMode.APPEND ? StandardOpenOption.APPEND : StandardOpenOption.WRITE);
    }

    /**
     * Returns the full file name including extension.
     *
     * @return Full file name.
     */
    public String getFullName() {
        return path.getFileName().toString();
    }

    /**
     * Get File Attributes
     *
     * @return the file's attributes.
     */
    public BasicFileAttributes fileAttributes() throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExtFile other)) return false;
        try {
            return path.toRealPath().equals(other.path.toRealPath());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return path.toRealPath().hashCode();
        } catch (IOException e) {
            return path.toAbsolutePath().hashCode();
        }
    }

    /**
     * Resolves a location starting at this file.
     *
     * @param fullName The full name of the file. Example: "hello.py"
     * @return the resolved file
     */
    public ExtFile resolve(String fullName) {
        return of(path.resolve(fullName));
    }


    public enum Sorters {
        TYPE_ASCENDING(Sorters::type, false), TYPE_DESCENDING(Sorters::type, true), CREATION_EARLIEST(Sorters::creationDate, false), CREATION_LATEST(Sorters::creationDate, true), NAME_ASCENDING(Sorters::name, false), NAME_DESCENDING(Sorters::name, true), MODIFICATION_EARLIEST(Sorters::modifiedDate, false), MODIFICATION_LATEST(Sorters::modifiedDate, true),
        ;


        final IOThrowingBiFunction<ExtFile, ExtFile, Integer> biFunction;
        final boolean reversed;

        Sorters(IOThrowingBiFunction<ExtFile, ExtFile, Integer> biFunction, boolean reversed) {
            this.biFunction = biFunction;
            this.reversed = reversed;
        }

        /**
         *
         * @return the comparison of the file with the lowest file extension. Directories are at the top.
         */
        public static int type(@NotNull ExtFile file1, ExtFile file2) {
            if (file1.isDir() && !file2.isDir()) {
                return -1;
            } else if (!file1.isDir() && file2.isDir()) {
                return 1;
            } else {
                return file1.getFileExtension().compareTo(file2.getFileExtension());
            }
        }

        /**
         *
         * @return the comparison of the file with the lowest creation date.
         */
        public static int creationDate(@NotNull ExtFile file1, @NotNull ExtFile file2) throws IOException {
            return file1.fileAttributes().creationTime().compareTo(file2.fileAttributes().creationTime());

        }


        /**
         *
         * @return the comparison of the file with the lowest modification date.
         */
        public static int modifiedDate(@NotNull ExtFile file1, @NotNull ExtFile file2) throws IOException {
            return file1.fileAttributes().lastModifiedTime().compareTo(file2.fileAttributes().lastModifiedTime());

        }

        /**
         *
         * @return the comparison of the file with the lowest modification date.
         */
        public static int name(@NotNull ExtFile file1, @NotNull ExtFile file2) {
            return file1.getName().compareTo(file2.getName());

        }

        public Comparator<ExtFile> get() {
            return (o1, o2) -> {
                int r;
                try {
                    r = biFunction.apply(o1, o2);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                if (reversed) {
                    if (r == 1) {
                        r = -1;
                    } else if (r == -1) {
                        r = 1;
                    }
                }
                return r;
            };

        }
    }


    public enum WriteMode {
        /**
         * Append at the end of a file.
         */
        APPEND,
        /**
         * Overwrite but don't truncate.
         */
        OVERWRITE,
        /**
         * Clear the file and write.
         */
        OVERWRITE_ALL,


    }


}
