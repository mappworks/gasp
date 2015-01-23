package gasp.core.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gasp.core.util.Json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Base class for model objects.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(Include.NON_NULL)
public class GaspObject {

    /**
     * object id
     */
    String id;

    /**
     * creator of the object.
     */
    String creator;

    /**
     * date/time of object creation.
     */
    @JsonSerialize(using = Json.DateSerializer.class)
    @JsonDeserialize(using = Json.DateDeserializer.class)
    Date created;

    /**
     * date/time of last object modification.
     */
    @JsonSerialize(using = Json.DateSerializer.class)
    @JsonDeserialize(using = Json.DateDeserializer.class)
    Date modified;

    /**
     * extensible metadata
     */
    Metadata meta;

    /**
     * tags
     */
    List<String> tags;

    /**
     * The object id.
     */
    public String id() {
        return id;
    }

        /**
     * Sets the object id.
     * <p>
     * Application code should never call this method.
     * </p>
     */
    public GaspObject id(String id) {
        this.id = id;
        return this;
    }

    /**
     * The object creator.
     */
    public String creator() {
        return creator;
    }

    /**
     * Sets the object creator.
     * <p>
     * Application code should never call this method.
     * </p>
     */
    public GaspObject creator(String creator) {
        this.creator = creator;
        return this;
    }

    /**
     * The object creation date.
     */
    public Date created() {
        return created;
    }

    /**
     * Sets the object creation date.
     * <p>
     * Application code should never call this method.
     * </p>
     */
    public GaspObject created(Date created) {
        this.created = created;
        return this;
    }

    /**
     * The object last modified date.
     */
    public Date modified() {
        return modified;
    }

    /**
     * Sets the object last modified date.
     * <p>
     * Application code should never call this method.
     * </p>
     */
    public GaspObject modified(Date modified) {
        this.modified = modified;
        return this;
    }

    /**
     * The extensible metadata object.
     */
    public Metadata meta() {
        if (meta == null) {
            meta = new Metadata();
        }
        return meta;
    }

    /**
     * Sets a set of metadata key value pairs.
     */
    public GaspObject meta(Map<String,Object> kvp) {
        meta().map().putAll(kvp);
        return this;
    }

    /**
     * List of tags associated with the object.
     */
    public List<String> tags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    /**
     * Associates a tag with the object.
     */
    public GaspObject tag(String tag) {
        tags().add(tag);
        return this;
    }

}
