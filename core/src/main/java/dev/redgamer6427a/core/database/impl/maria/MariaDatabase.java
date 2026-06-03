package dev.redgamer6427a.core.database.impl.maria;

import java.sql.SQLException;
import java.util.function.Consumer;

public abstract class MariaDatabase extends dev.redgamer6427a.core.database.core.Database {

    /**
     *
     * @param URL JDBC connection URL.
     * @param username Account user.
     * @param password Account password.
     * @param errorHandler Handles SQLExceptions.
     */
    protected MariaDatabase(String URL, String username, String password, Consumer<SQLException> errorHandler) {
        super(URL, username, password, errorHandler);
    }

    /**
     * Behaves the same as the other constructor with the only diffrence being that it constructs the JDBC URL itself.
     * @param host Host (Example: localhost).
     * @param port Port (Example: 3306).
     * @param name Name (Example: my_database).
     * @param username Account user.
     * @param password Account password
     * @param errorHandler Handles SQLExceptions.
     */
    protected MariaDatabase(String host, int port, String name, String username, String password, Consumer<SQLException> errorHandler) {
        super("jdbc:mariadb://" + host + ":" + port + "/" + name,  username, password, errorHandler);
    }


}
