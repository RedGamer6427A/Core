package dev.redgamer6427a.core.database.impl.sqlite;

import dev.redgamer6427a.core.database.core.Database;
import dev.redgamer6427a.core.files.ExtFile;
import dev.redgamer6427a.core.logging.Logger;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.function.Consumer;


public abstract class SQLiteDatabase extends Database {
    private final Path path;

    Logger logger = Logger.create();

    /**
     *
     * @param URL JDBC connection URL.
     * @param username Account user.
     * @param password Account password.
     * @param errorHandler Handles SQLExceptions.
     */
    protected SQLiteDatabase(String URL, String username, String password, Consumer<SQLException> errorHandler) {
        super(URL, username, password, errorHandler);
        path = Paths.get(URL.substring(URL.indexOf(':', URL.indexOf(':') + 1) + 1));

    }

    /**
     * Behaves the same as the other constructor with the only difference being that it constructs the JDBC URL itself.
     * @param errorHandler Handles SQLExceptions.
     */
    protected SQLiteDatabase(String path, Consumer<SQLException> errorHandler) {
        super("jdbc:sqlite:"+path, "", "", errorHandler);
        this.path = Paths.get(path);
    }

    @SneakyThrows
    @Override
    public void connect() {
        ExtFile extFile = ExtFile.of(path);
        if (!extFile.exists()) {
            extFile.create();
        }

        super.connect();
    }
}
