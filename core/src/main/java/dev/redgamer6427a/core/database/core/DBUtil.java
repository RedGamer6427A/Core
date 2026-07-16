package dev.redgamer6427a.core.database.core;

import com.google.gson.Gson;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class DBUtil {

    public enum Dialect { MARIA, SQLITE }

    private static final Gson gson = new Gson();
    private DBUtil() {}

    public static String getColumnType(Class<?> clazz, Dialect dialect) {
        if (clazz == String.class)                       return dialect == Dialect.SQLITE ? "TEXT"        : "VARCHAR(255)";
        if (clazz == int.class || clazz == Integer.class) return "INT";
        if (clazz == long.class || clazz == Long.class)  return "BIGINT";
        if (clazz == boolean.class || clazz == Boolean.class) return "BOOLEAN";
        if (clazz == double.class || clazz == Double.class)   return "DOUBLE";
        if (clazz == float.class || clazz == Float.class)     return "FLOAT";
        if (clazz == byte.class || clazz == Byte.class)       return "TINYINT";
        if (clazz == short.class || clazz == Short.class)     return "SMALLINT";
        if (clazz == char.class || clazz == Character.class)  return dialect == Dialect.SQLITE ? "TEXT"   : "CHAR(1)";
        if (clazz == UUID.class)                         return dialect == Dialect.SQLITE ? "TEXT"        : "UUID";
        if (clazz == java.time.Instant.class)            return "BIGINT";
        if (clazz == Date.class)                         return "BIGINT";
        if (clazz == java.time.LocalDate.class)          return dialect == Dialect.SQLITE ? "TEXT"        : "DATE";
        if (clazz == java.time.LocalDateTime.class)      return dialect == Dialect.SQLITE ? "TEXT"        : "DATETIME";
        if (clazz == java.time.OffsetDateTime.class)     return dialect == Dialect.SQLITE ? "TEXT"        : "DATETIME";
        if (clazz == java.math.BigDecimal.class)         return dialect == Dialect.SQLITE ? "TEXT"        : "DECIMAL(30,10)";
        if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz))
            return dialect == Dialect.SQLITE ? "TEXT"        : "JSON";
        return dialect == Dialect.SQLITE ? "TEXT" : "JSON";
    }


    /**
     * Turns an object into a MariaDB-compatible one. Uses GSON for complex types.
     *
     * @param object The Object to convert.
     * @return the MariaDB value.
     */
    public static String compileValue(Object object) {
        switch (object) {
            case null -> {
                return "NULL";
            }
            case String s -> {


                return "'" + object.toString().replace("'", "''") + "'";
            }
            case UUID uuid -> {
                return object.toString().replace("'", "''");
            }
            case Boolean b -> {
                return b ? "1" : "0";
            }
            case Number number -> {
                return object.toString();
            }
            case java.time.Instant instant -> {
                return String.valueOf(instant.toEpochMilli());
            }
            case Date date -> {
                return String.valueOf(date.getTime());
            }
            case java.time.LocalDate localDate -> {
                return "'" + object + "'";
            }
            case java.time.LocalDateTime localDateTime -> {
                return "'" + object.toString().replace("T", " ") + "'";
            }
            default -> {
            }
        }

        // Collections and other objects: serialize to JSON


        return gson.toJson(object).replace("'", "''");
    }

    public static Object bindValue(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s;
        if (value instanceof UUID uuid) return uuid.toString();
        if (value instanceof Boolean b) return b; // let driver handle, or b ? 1 : 0 if needed
        if (value instanceof Number) return value;
        if (value instanceof java.time.Instant instant) return instant.toEpochMilli();
        if (value instanceof Date date) return date.getTime();
        if (value instanceof java.time.LocalDate || value instanceof java.time.LocalDateTime)
            return value.toString().replace("T", " ");
        if (value instanceof java.math.BigDecimal bd) return bd.toString();
        return gson.toJson(value); // collections/complex objects → plain JSON, no SQL quoting
    }

    /**
     * Parse the MariaDB Object into a Java Object
     *
     * @param value  the value.
     * @param column the column that value was in.
     * @param <T>    Value type.
     * @return the typed value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(String value, Table.TableColumn column) {
        Class<?> type = column.type();

        if (value == null) return null;

        if (type == String.class) return (T) value;
        if (type == Integer.class || type == int.class) return (T) Integer.valueOf(value);
        if (type == Long.class || type == long.class) return (T) Long.valueOf(value);
        if (type == Boolean.class || type == boolean.class)
            return (T) Boolean.valueOf(value.equals("1") || value.equalsIgnoreCase("true"));
        if (type == Double.class || type == double.class) return (T) Double.valueOf(value);
        if (type == Float.class || type == float.class) return (T) Float.valueOf(value);
        if (type == Byte.class || type == byte.class) return (T) Byte.valueOf(value);
        if (type == Short.class || type == short.class) return (T) Short.valueOf(value);
        if (type == Character.class || type == char.class) return (T) Character.valueOf(value.charAt(0));
        if (type == UUID.class) return (T) UUID.fromString(value);
        if (type == java.time.Instant.class) return (T) java.time.Instant.ofEpochMilli(Long.parseLong(value));
        if (type == Date.class) return (T) new Date(Long.parseLong(value));
        if (type == java.time.LocalDate.class) return (T) java.time.LocalDate.parse(value);
        if (type == java.time.LocalDateTime.class) return (T) java.time.LocalDateTime.parse(value.replace(" ", "T"));
        if (type == java.math.BigDecimal.class) return (T) new java.math.BigDecimal(value);

        // Collections and other objects: deserialize JSON
        return (T) gson.fromJson(value, type);
    }


}
