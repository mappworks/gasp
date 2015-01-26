package gasp.core.db;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Locale;

import static java.lang.String.format;

/**
 * Database table column.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Column {

    String name;
    String type;

    @JsonIgnore
    int sqlType;

    /**
     * Name of the column.
     */
    public String name() {
        return name;
    }

    /**
     * Sets the name of the column.
     */
    public Column name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Type name of the column.
     */
    public String type() {
        return type;
    }

    /**
     * Sets the type name of the column.
     */
    public Column type(String type) {
        this.type = type;
        return this;
    }

    /**
     * SQL type of the column.
     *
     * @see java.sql.Types
     */
    public int sqlType() {
        return sqlType;
    }

    /**
     * Sets the sql type of the column.
     *
     * @see java.sql.Types
     */
    public Column sqlType(int sqlType) {
        this.sqlType = sqlType;
        return this;
    }

    @Override
    public String toString() {
        return format(Locale.ROOT, "%s(%s)", name, type);
    }
}
