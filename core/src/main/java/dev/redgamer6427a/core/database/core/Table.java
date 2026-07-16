package dev.redgamer6427a.core.database.core;

import com.google.gson.Gson;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.utils.LiveMap;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Consumer;

/**
 * A High-Level No-SQL MariaDB Table implementation.
 *
 * @param <K> The Key/Primary Type.
 * @param <R> The Record to indicate the database fields.
 */
public abstract class Table<K, R extends Record> {

    @Getter
    private final String tableName;
    private final Database database;
    private final Class<R> recordClass;
    private final Class<K> keyClass;
    private final Consumer<SQLException> errorHandler;
    private final DBUtil.Dialect dialect;

    /**
     *
     * @param tableName   The name of the table.
     * @param database    The parent database.
     * @param keyClass    The class of the Key
     * @param recordClass The class of the Record.
     */
    protected Table(String tableName, Database database, Class<K> keyClass, Class<R> recordClass, DBUtil.Dialect dialect) {
        this.tableName = tableName;
        this.database = database;
        this.errorHandler = database.getErrorHandler();
        this.recordClass = recordClass;
        this.keyClass = keyClass;
        this.dialect = dialect;
    }

    protected abstract String buildUpsertSuffix(List<TableColumn> columns);

    private static final Gson gson = new Gson();

    /**
     * Parse the MariaDB Object into a Java Object
     *
     * @param value the value.
     * @return the typed value.
     */
    @SuppressWarnings("unchecked")
    private K getKeyValue(String value) {
        Class<?> type = this.keyClass;

        if (value == null) return null;

        if (type == String.class) return (K) value;
        if (type == Integer.class || type == int.class) return (K) Integer.valueOf(value);
        if (type == Long.class || type == long.class) return (K) Long.valueOf(value);
        if (type == Boolean.class || type == boolean.class)
            return (K) Boolean.valueOf(value.equals("1") || value.equalsIgnoreCase("true"));
        if (type == Double.class || type == double.class) return (K) Double.valueOf(value);
        if (type == Float.class || type == float.class) return (K) Float.valueOf(value);
        if (type == Byte.class || type == byte.class) return (K) Byte.valueOf(value);
        if (type == Short.class || type == short.class) return (K) Short.valueOf(value);
        if (type == Character.class || type == char.class) return (K) Character.valueOf(value.charAt(0));
        if (type == UUID.class) return (K) UUID.fromString(value);
        if (type == java.time.Instant.class) return (K) java.time.Instant.ofEpochMilli(Long.parseLong(value));
        if (type == Date.class) return (K) new Date(Long.parseLong(value));
        if (type == java.time.LocalDate.class) return (K) java.time.LocalDate.parse(value);
        if (type == java.time.LocalDateTime.class) return (K) java.time.LocalDateTime.parse(value.replace(" ", "T"));
        if (type == java.math.BigDecimal.class) return (K) new java.math.BigDecimal(value);

        // Collections and other objects: deserialize JSON
        return (K) gson.fromJson(value, type);
    }

    /**
     * Ensures the table exists.
     */
    private boolean ensured = false;

    public void ensureExists() {
        if (ensured) return;
        List<TableColumn> columns = getTableColumns();
        database.ensureConnected();

        String idType = DBUtil.getColumnType(keyClass, dialect);

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (");

        sql.append("`id` ").append(idType).append(" PRIMARY KEY");

        for (TableColumn col : columns) {
            sql.append(", `").append(col.name()).append("` ").append(DBUtil.getColumnType(col.type(), dialect));
        }

        sql.append(");");

        String finalSql = sql.toString();
        logStatement(finalSql);
        try (Statement stmt = database.getConnection().createStatement()) {
            stmt.executeUpdate(finalSql);
        } catch (SQLException e) {
            errorHandler.accept(e);
        }

        ensured = true;
    }

    /**
     * Put a value into the table.
     *
     * @param key the key.
     * @param rec the value.
     */
    public void put(K key, R rec) {
        ensureExists();
        List<TableColumn> columns = getTableColumns();
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT OR REPLACE INTO ").append(tableName).append(" (id, ");


        for (TableColumn col : columns) {
            sql.append(col.name());
            if (!col.equals(columns.getLast())) {
                sql.append(", ");
            }
        }
        sql.append(") VALUES (?, ");
        for (TableColumn col : columns) {
            sql.append("?");
            if (!col.equals(columns.getLast())) {
                sql.append(", ");
            }
        }
        sql.append(buildUpsertSuffix(columns));
        logStatementPrepare(sql.toString());
        try (PreparedStatement stmt = database.getConnection().prepareStatement(sql.toString())) {
            stmt.setObject(1, key);
            int i = 1;
            for (RecordComponent rc : rec.getClass().getRecordComponents()) {
                i++;
                try {
                    Object value = rc.getAccessor().invoke(rec);
                    Object bindValue = (value == null || value instanceof Number || value instanceof Boolean) ? value : (value instanceof String s) ? s : DBUtil.bindValue(value); // see below
                    stmt.setObject(i, bindValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    i--;
                }
            }
            logStatement(stmt);
            stmt.executeUpdate();

        } catch (SQLException e) {
            errorHandler.accept(e);
        }
    }

    /**
     * Get a value from the table.
     *
     * @param key the key.
     * @return the value.
     */
    public R get(K key) {
        ensureExists();

        try (PreparedStatement stmt = database.getConnection().prepareStatement("SELECT * FROM " + tableName + " WHERE id = ?")) {

            stmt.setObject(1, key);
            logStatement(stmt);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return null;

            return makeInstance(rs);

        } catch (SQLException e) {
            errorHandler.accept(e);
            return null;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for parsing the record into a usable list.
     *
     * @return a list of TableColumns that are easier to work with.
     */
    private List<TableColumn> getTableColumns() {
        RecordComponent[] components = recordClass.getRecordComponents();
        List<TableColumn> columns = new ArrayList<>();
        for (RecordComponent rc : components) {
            columns.add(new TableColumn(rc.getName(), rc.getType()));
        }
        return columns;
    }

    /**
     * Create a snapshot of the current state of the table.
     *
     * @return a snapshot of the current state of the table.
     */
    public Map<K, R> snapshot() {
        ensureExists();
        Map<K, R> map = new LinkedHashMap<>();
        String sql = "SELECT * FROM " + tableName;
        logStatement(sql);
        try (PreparedStatement stmt = database.getConnection().prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                K key = getKeyValue(rs.getString("id"));

                R record = makeInstance(rs);
                map.put(key, record);
            }

        } catch (SQLException e) {
            errorHandler.accept(e);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    private R makeInstance(ResultSet rs) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<Object> values = new ArrayList<>();
        List<TableColumn> columns = getTableColumns();
        for (TableColumn col : columns) {
            String strVal = rs.getString(col.name());
            values.add(DBUtil.getValue(strVal, col));
        }

        Class<?>[] paramTypes = columns.stream().map(TableColumn::type).toArray(Class<?>[]::new);

        Constructor<R> rConstructor = recordClass.getConstructor(paramTypes);
        return rConstructor.newInstance(values.toArray());
    }

    /**
     * Returns a LiveMap object that is up-to-date.
     *
     * @return a LiveMap of the table.
     */
    public Map<K, R> liveMap() {
        return new LiveMap<>(this::snapshot, new LiveMap.MapChangeListener<>() {
            @Override
            public void onPut(K key, R value) {
                put(key, value);
            }

            @Override
            public void onRemove(Object key, R oldValue) {
                if (keyClass.isAssignableFrom(key.getClass())) {
                    delete(keyClass.cast(key));
                }
            }
        });
    }

    /**
     * Returns a key set.
     *
     * @return A key set.
     */
    @SuppressWarnings("unchecked")
    public Set<K> keySet() {
        ensureExists();
        Set<K> keys = new LinkedHashSet<>();
        String sql = "SELECT id FROM " + tableName;
        logStatement(sql);
        try (PreparedStatement stmt = database.getConnection().prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) keys.add((K) rs.getObject("id"));

        } catch (SQLException e) {
            errorHandler.accept(e);
        }

        return keys;
    }

    /**
     * Returns all values.
     *
     * @return all values.
     */
    public List<R> values() {
        ensureExists();
        List<R> list = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        logStatement(sql);
        try (PreparedStatement stmt = database.getConnection().prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                list.add(makeInstance(rs));
            }

        } catch (SQLException e) {
            errorHandler.accept(e);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /**
     * Delete a row.
     *
     * @param key the key of that row.
     */
    public void delete(K key) {
        ensureExists();
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement stmt = database.getConnection().prepareStatement(sql)) {
            stmt.setObject(1, key);
            logStatement(stmt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
    }

    /**
     * Checks if a key exists.
     *
     * @param key the key to check.
     * @return whether the key is in the database.
     */
    public boolean exists(K key) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement stmt = database.getConnection().prepareStatement(sql)) {
            stmt.setObject(1, key);
            logStatement(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            errorHandler.accept(e);
            return false;
        }
    }

    /**
     * Returns the amount of rows.
     *
     * @return the amount of rows.
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        logStatement(sql);
        try (Statement stmt = database.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            errorHandler.accept(e);
            return 0;
        }
    }

    /**
     * A helper object for columns.
     *
     * @param name the name of the column.
     * @param type the type of the column.
     */
    public record TableColumn(String name, Class<?> type) {
    }

    private static final Logger logger = Logger.create();

    private static void logStatement(Statement stmt) {
        logStatement(stmt.toString());
    }

    private static void logStatement(String sql) {
        logger.finest("Executing SQL: " + sql);
    }


    private static void logStatementPrepare(String sql) {
        logger.finest("Preparing SQL: " + sql);
    }


}
