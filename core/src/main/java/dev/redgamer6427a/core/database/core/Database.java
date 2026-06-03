package dev.redgamer6427a.core.database.core;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;

public abstract class Database {

    private final String URL;

    private final String username;
    private final String password;
    @Getter
    private final Consumer<SQLException> errorHandler;

    @Getter
    private Connection connection;

    /**
     *
     * @param URL JDBC connection URL.
     * @param username Account user.
     * @param password Account password.
     * @param errorHandler Handles SQLExceptions.
     */
    protected Database(String URL, String username, String password, Consumer<SQLException> errorHandler) {
        this.URL = URL;

        this.username = username;
        this.password = password;
        this.errorHandler = errorHandler;
    }


    /**
     * (Re)connect to the database.
     */
    public void connect() {
        try {
            if (isConnected()) disconnect();
            connection = DriverManager.getConnection(URL, username, password);
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
    }

    /**
     * Close the connection. Silently returns if it is already closed.
     */
    public void disconnect() {
        try {
            if (!isConnected()) {
                connection = null;
                return;
            }
            connection.close();
            connection = null;
        } catch (SQLException e) {
            errorHandler.accept(e);
        }


    }

    /**
     * Check whether the connection to the database is active.
     * @return whether the connection to the database is active.
     */
    public boolean isConnected() {
        try {
            return !(connection == null || connection.isClosed() || !connection.isValid(2));
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
        return connection != null;
    }

    /**
     * Reconnects if there is no connection or it is invalid.
     */
    public void ensureConnected() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connect();
            }
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
    }


}
