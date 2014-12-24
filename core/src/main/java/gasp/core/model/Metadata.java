package gasp.core.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

/**
 * Extensible map of key value pairs.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Metadata {

    /**
     * the key/value pair map
     */
    Map<String,Object> map = Maps.newLinkedHashMap();

    /**
     * Returns the key value pair map.
     */
    public Map<String,Object> map() {
        return map;
    }

    /**
     * Sets a metadata value.
     *
     * @param key The key.
     * @param val The value.
     *
     * @return This object.
     */
    public Metadata set(String key, Object val) {
        map.put(key, val);
        return this;
    }

    /**
     * Gets a metadata value.
     *
     * @param key The key.
     *
     * @return The optional value.
     */
    public Optional<Object> get(String key) {
        return Optional.ofNullable(map.get(key));
    }
}
