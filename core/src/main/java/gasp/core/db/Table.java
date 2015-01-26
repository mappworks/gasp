package gasp.core.db;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.List;

/**
 * A database table.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(Include.NON_EMPTY)
public class Table {

    String name;
    String schema;
    String type;
    List<Column> columns = new ArrayList();

    /**
     * The table name.
     */
    public String name() {
        return name;
    }

    /**
     * Sets the table name.
     */
    public Table name(String name) {
        this.name = name;
        return this;
    }

    /**
     * The schema name of the table.
     */
    public String schema() {
        return schema;
    }

    /**
     * Sets the schema name of the table.
     */
    public Table schema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * The table type.
     */
    public String type() {
        return type;
    }

    /**
     * Sets the table type.
     */
    public Table type(String type) {
        this.type = type;
        return this;
    }

    /**
     * List of columns of the table.
     */
    public List<Column> columns() {
        return columns;
    }

    /**
     * Adds a column to the table column list.
     */
    public Table column(Column col) {
        columns.add(col);
        return this;
    }
}
