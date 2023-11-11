package io.xdb.core.persistence.to_load;

import io.xdb.gen.models.TableReadLockingPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PersistenceTableSchemaToLoad {

    private final String tableName;
    private final List<String> columnNames;
    private final TableReadLockingPolicy tableReadLockingPolicy;

    /**
     * Create a table schema to load with the default NO_LOCKING reading policy.
     *
     * @param tableName     table name.
     * @param columnNames   the columns to load.
     * @return  the created table schema to load.
     */
    public static PersistenceTableSchemaToLoad create(final String tableName, final String... columnNames) {
        return PersistenceTableSchemaToLoad.create(
            tableName,
            Arrays.stream(columnNames).collect(Collectors.toList()),
            TableReadLockingPolicy.NO_LOCKING
        );
    }

    /**
     * Create a table schema to load with the default NO_LOCKING reading policy.
     *
     * @param tableName     table name.
     * @param columnNames   the columns to load.
     * @return  the created table schema to load.
     */
    public static PersistenceTableSchemaToLoad create(final String tableName, final List<String> columnNames) {
        return PersistenceTableSchemaToLoad.create(tableName, columnNames, TableReadLockingPolicy.NO_LOCKING);
    }

    /**
     * Create a table schema to load.
     *
     * @param tableName                 table name.
     * @param columnNames               the columns to load.
     * @param tableReadLockingPolicy    locking policy when reading the table.
     * @return  the created table schema to load.
     */
    public static PersistenceTableSchemaToLoad create(
        final String tableName,
        final List<String> columnNames,
        final TableReadLockingPolicy tableReadLockingPolicy
    ) {
        return new PersistenceTableSchemaToLoad(tableName, columnNames, tableReadLockingPolicy);
    }
}
