package jasp.core.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Parameter of a {@link jasp.core.model.Dataset} query.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class QueryParam {

    public static enum Type {
        String, Number, Date
    }

    String name;

    Type type = Type.String;
    Object defaultValue;

    public String name() {
        return name;
    }

    public QueryParam name(String name) {
        this.name = name;
        return this;
    }

    public Type type() {
        return type;
    }

    public QueryParam type(Type type) {
        this.type = type;
        return this;
    }

    public Object defaultValue() {
        return defaultValue;
    }

    public QueryParam defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
}
