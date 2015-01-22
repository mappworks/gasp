package gasp.core.db;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Locale;

import static java.lang.String.format;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Column {

    final String name;
    final String type;

    @JsonIgnore
    final int sqlType;

    public Column(String name, String type, int sqlType) {
        this.name = name;
        this.type = type;
        this.sqlType = sqlType;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public int sqlType() {
        return sqlType;
    }

    @Override
    public String toString() {
        return format(Locale.ROOT, "%s(%s)", name, type);
    }
}
