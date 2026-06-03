package dev.redgamer6427a.core.database.impl.maria;

import dev.redgamer6427a.core.database.core.DBUtil;
import dev.redgamer6427a.core.database.core.Table;

import java.util.List;

/**
 * A High-Level No-SQL MariaDB Table implementation.
 *
 * @param <K> The Key/Primary Type.
 * @param <R> The Record to indicate the database fields.
 */
public abstract class MariaTable<K, R extends Record> extends Table<K, R> {

    /**
     *
     * @param tableName   The name of the table.
     * @param mariaDatabase    The parent database.
     * @param keyClass    The class of the Key
     * @param recordClass The class of the Record.
     */
    protected MariaTable(String tableName, MariaDatabase mariaDatabase, Class<K> keyClass, Class<R> recordClass) {
        super(tableName, mariaDatabase, keyClass, recordClass, DBUtil.Dialect.MARIA);
    }

    @Override
    protected String buildUpsertSuffix(List<TableColumn> columns) {
        StringBuilder sb = new StringBuilder(" ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < columns.size(); i++) {
            String name = columns.get(i).name();
            sb.append("`").append(name).append("` = VALUES(`").append(name).append("`)");
            if (i < columns.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }


}
