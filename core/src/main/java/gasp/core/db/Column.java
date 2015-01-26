package gasp.core.db;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Locale;

import static java.lang.String.format;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Column {

    String name;
    String type;

    @JsonIgnore
    int sqlType;

    public String name() {
        return name;
    }

    public Column name(String name) {
        this.name = name;
        return this;
    }

    public String type() {
        return type;
    }

    public Column type(String type) {
        this.type = type;
        return this;
    }

    public int sqlType() {
        return sqlType;
    }

    public Column sqlType(int sqlType) {
        this.sqlType = sqlType;
        return this;
    }

    @Override
    public String toString() {
        return format(Locale.ROOT, "%s(%s)", name, type);
    }
}
