package jasp.core.model;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

/**
 * Extensible map of key value pairs.
 */
public class Metadata {

    /**
     * the key/value pair map
     */
    Map<String,Object> map = Maps.newLinkedHashMap();

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
