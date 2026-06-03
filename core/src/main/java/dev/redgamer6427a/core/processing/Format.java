package dev.redgamer6427a.core.processing;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.StreamSupport;

public class Format {

    private static final List<String> SI_PREFIXES = List.of("k", "M", "G", "T", "P", "E", "Z", "Y");
    private static final List<String> IEC_PREFIXES = List.of("Ki", "Mi", "Gi", "Ti", "Pi", "Ei", "Zi", "Yi");

    /**
     * Format a date.
     *
     * @param date   the date.
     * @param format the format.
     * @return the formatted date.
     * @see DateFormats
     */
    public static String formatDate(Date date, DateFormats format) {
        return format.getFormatter().format(date);
    }

    /**
     * Format a date.
     *
     * @param timestamp the timestamp.
     * @param format    the format.
     * @return the formatted date.
     * @see DateFormats
     */
    public static String formatDate(long timestamp, DateFormats format) {
        return formatDate(new Date(timestamp), format);
    }

    /**
     * Format a date.
     *
     * @param date   the date.
     * @param format the format.
     * @return the formatted date.
     * @see DateFormats
     */
    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Format a date.
     *
     * @param timestamp the timestamp.
     * @param format    the format.
     * @return the formatted date.
     * @see DateFormats
     */
    public static String formatDate(long timestamp, String format) {
        return formatDate(new Date(timestamp), format);
    }

    public static String formatSize(long bytes) {
        if (bytes < 1000) return bytes + " B";

        int stage = 0;
        double size = bytes;
        while (size >= 1000 && stage < SI_PREFIXES.size()) {
            size /= 1000;
            stage++;
        }

        return trimDecimals(size) + " " + SI_PREFIXES.get(stage - 1) + "B";
    }

    public static String formatSizeIEC(long bytes) {
        if (bytes < 1024) return bytes + " B";

        int stage = 0;
        double size = bytes;
        while (size >= 1024 && stage < IEC_PREFIXES.size()) {
            size /= 1024;
            stage++;
        }

        return trimDecimals(size) + " " + IEC_PREFIXES.get(stage - 1) + "B";
    }

    // Removes unnecessary ".0" or trailing zeros
    private static String trimDecimals(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number); // integer, no decimals
        } else {
            return String.format("%.2f", number); // keep decimal
        }
    }

    public static String toString(Object o, ToStringParameters parameters, Object root) throws IllegalAccessException {
        return toString(o, parameters, root, new IdentityHashMap<>());
    }

    private static String toString(Object o, ToStringParameters parameters, Object root, Map<Object, Boolean> visited) throws IllegalAccessException {

        if (o == null) return "null";

        // cycle detection


        Class<?> clazz = o.getClass();
        if (clazz == String.class) return "\"" + o + "\"";
        if (clazz == Character.class) return "'" + o + "'";


        // primitives, wrappers, String
        if (clazz.isPrimitive() || clazz == Integer.class || clazz == Long.class || clazz == Byte.class || clazz == Boolean.class || clazz == Double.class || clazz == Float.class || clazz == Short.class) {
            return String.valueOf(o);
        }

        if (root != null && root.equals(o)) return "this";

        if ((o instanceof Enum<?> e)) {
            return e.name();
        }

        // Iterable
        if (o instanceof Iterable<?> iter) {
            List<?> list = StreamSupport.stream(iter.spliterator(), false).toList();
            StringBuilder sb = new StringBuilder();
            sb.append('[');

            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);

                if (item == o) {
                    sb.append("this");
                } else {
                    sb.append(toString(item, parameters, root == null ? o : root, visited));
                }

                if (i != list.size() - 1) sb.append(", ");
            }

            sb.append(']');
            return sb.toString();
        }

        // Map
        if (o instanceof Map<?, ?> map) {
            List<? extends Map.Entry<?, ?>> entries = map.entrySet().stream().toList();

            StringBuilder sb = new StringBuilder();
            sb.append('{');

            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<?, ?> e = entries.get(i);

                Object key = e.getKey();
                Object val = e.getValue();

                if (key == o) {
                    sb.append("this");
                } else {
                    sb.append(toString(key, parameters, root == null ? o : root, visited));
                }

                sb.append(": ");

                if (val == o) {
                    sb.append("this");
                } else {
                    sb.append(toString(val, parameters, root == null ? o : root, visited));
                }

                if (i != entries.size() - 1) sb.append(", ");
            }

            sb.append('}');
            return sb.toString();
        }

        // object fields
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null) {
            for (Field f : current.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    fields.add(f);
                } catch (Exception ignored) {
                }
            }
            current = current.getSuperclass();
        }

        // filter fields
        List<Field> filtered = new ArrayList<>();
        for (Field field : fields) {
            int mod = field.getModifiers();

            if (Modifier.isStatic(mod)) continue;

            if ((parameters.restrictFinalFields && Modifier.isFinal(mod)) || (parameters.restrictPrivateFields && Modifier.isPrivate(mod)) || (parameters.restrictProtectedFields && Modifier.isProtected(mod)) || (parameters.restrictPublicFields && Modifier.isPublic(mod))) {
                continue;
            }

            filtered.add(field);
        }

        StringBuilder sb = new StringBuilder();
        if (root != null) sb.append("[");

        for (int i = 0; i < filtered.size(); i++) {
            Field f = filtered.get(i);
            sb.append(f.getName()).append("=");

            Object value;
            try {
                value = f.get(o);
            } catch (Exception e) {
                value = "<inaccessible>";
            }

            if (value == o) {
                sb.append("this");
            } else {
                sb.append(toString(value, parameters, root == null ? o : root, visited));
            }

            if (i != filtered.size() - 1) sb.append(", ");
        }

        if (root != null) sb.append("]");

        return sb.toString();
    }

    public static String toString(Object o, ToStringParameters parameters) throws IllegalAccessException {
        return toString(o, parameters, null);
    }

    public static String toString(Object o) throws IllegalAccessException {
        return toString(o, ToStringParameters.defaultParameters());
    }

    public static boolean hasCustomToString(Class<?> cls) {
        try {
            cls.getDeclaredMethod("toString");
            return true; // class declares its own toString()
        } catch (NoSuchMethodException e) {
            return false; // inherits Object.toString()
        }
    }

    /**
     * Format a number.
     *
     * @param number        the number
     * @param grouping      the separator every three digits (using ': 1'234'5.67)
     * @param floatingComma what floating point to use: "." (false) or "," (true)
     * @param base          the number's base.
     * @return the formatted number.
     */
    public String number(Number number, @Nullable String grouping, boolean floatingComma, int base) {
        if (number == null) return "null";

        String sep = floatingComma ? "," : ".";
        boolean isInteger = number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long;

        String result;

        if (isInteger) {
            long value = number.longValue();
            result = Long.toString(value, base);
            if (grouping != null && base == 10) { // only apply grouping in base 10
                result = addGrouping(result, grouping);
            }
        } else {
            double d = number.doubleValue();
            if (base == 10) {
                result = Double.toString(d);
                // Replace dot with comma if needed
                if (floatingComma) result = result.replace('.', ',');
                if (grouping != null) {
                    int idx = result.indexOf(sep);
                    String intPart = idx >= 0 ? result.substring(0, idx) : result;
                    String fracPart = idx >= 0 ? result.substring(idx) : "";
                    intPart = addGrouping(intPart, grouping);
                    result = intPart + fracPart;
                }
            } else {
                // Non-decimal bases: integer part + approximate fractional part
                long integerPart = (long) d;
                double fracPart = d - integerPart;

                result = Long.toString(integerPart, base);

                if (fracPart != 0) {
                    result += sep;
                    for (int i = 0; i < 10 && fracPart != 0; i++) {
                        fracPart *= base;
                        int digit = (int) fracPart;
                        result += Integer.toString(digit, base);
                        fracPart -= digit;
                    }
                }
            }
        }

        return result;
    }

    // Helper to add grouping for decimal integers
    private String addGrouping(String number, String grouping) {
        StringBuilder sb = new StringBuilder();
        int len = number.length();
        int count = 0;
        boolean negative = number.startsWith("-");
        for (int i = len - 1; i >= (negative ? 1 : 0); i--) {
            sb.insert(0, number.charAt(i));
            count++;
            if (count % 3 == 0 && i != (negative ? 1 : 0)) {
                sb.insert(0, grouping);
            }
        }
        if (negative) sb.insert(0, '-');
        return sb.toString();
    }

    /**
     * Format a number.
     *
     * @param number        the number
     * @param grouping      the separator every three digits (using ': 1'234'5.67)
     * @param floatingComma what floating point to use: "." (false) or "," (true)
     * @return the formatted number.
     */
    public String number(Number number, @Nullable String grouping, boolean floatingComma) {
        return number(number, grouping, floatingComma, 10);
    }

    public static String padding(String prime, String... others) {
        int len = 0;

        for (String other : others) {
            if (other.length() > len) len = other.length();
        }

        return " ".repeat(len - prime.length());

    }


    @Getter
    public enum DateFormats {
        // ISO formats
        ISO_8601("yyyy-MM-dd'T'HH:mm:ss'Z'"),
        ISO_8601_MS("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        ISO_DATE("yyyy-MM-dd"),
        ISO_TIME("HH:mm:ss"),
        ISO_TIME_MS("HH:mm:ss.SSS"),

        // RFC formats
        RFC_1123("EEE, dd MMM yyyy HH:mm:ss zzz"),
        RFC_3339("yyyy-MM-dd'T'HH:mm:ssXXX"),

        // Long / Full formats
        FULL_DATE("EEEE, MMMM dd, yyyy"),
        FULL_DATE_TIME("EEEE, MMMM dd, yyyy HH:mm:ss"),
        LONG_DATE("MMMM dd, yyyy"),
        LONG_DATE_TIME("MMMM dd, yyyy HH:mm:ss"),

        // Short / medium formats
        SHORT_DATE("dd/MM/yyyy"),
        SHORT_DATE_TIME("dd/MM/yyyy HH:mm"),
        MEDIUM_DATE("MMM dd, yyyy"),
        MEDIUM_DATE_TIME("MMM dd, yyyy HH:mm:ss"),

        // Time only
        SHORT_TIME("HH:mm"),
        LONG_TIME("HH:mm:ss"),
        FULL_TIME("HH:mm:ss.SSS"),

        // Misc
        COMPACT("ddMMyyyyHHmmss"),       // good for filenames
        YEAR_MONTH("MM-yyyy"),           // year and month only
        DAY_MONTH("dd-MM"),              // month and day only
        LOGS("dd.MM.yyyy-HH:mm:ss"),
        LOGS_DATE("dd-MM-yyyy"),
        ;

        private final String pattern;

        DateFormats(String pattern) {
            this.pattern = pattern;
        }

        public SimpleDateFormat getFormatter() {
            return new SimpleDateFormat(pattern);
        }
    }

    public record ToStringParameters(boolean restrictPrivateFields, boolean restrictProtectedFields,
                                     boolean restrictPublicFields, boolean restrictFinalFields) {
        public static ToStringParameters defaultParameters() {
            return new ToStringParameters(false, false, false, false);
        }
    }
}
 