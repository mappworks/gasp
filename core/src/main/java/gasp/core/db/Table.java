package gasp.core.db;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(Include.NON_EMPTY)
public class Table {

    String name;
    String schema;
    String type;
    List<Column> columns = new ArrayList();

    public String name() {
        return name;
    }

    public Table name(String name) {
        this.name = name;
        return this;
    }

    public String schema() {
        return schema;
    }

    public Table schema(String schema) {
        this.schema = schema;
        return this;
    }

    public String type() {
        return type;
    }

    public Table type(String type) {
        this.type = type;
        return this;
    }

    public List<Column> columns() {
        return columns;
    }

    public Table column(Column col) {
        columns.add(col);
        return this;
    }
}
