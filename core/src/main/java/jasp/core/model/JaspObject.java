package jasp.core.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jasp.core.util.Json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Base class for model objects.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(Include.NON_NULL)
public class JaspObject {

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
    List<Tag> tags;

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
    public JaspObject id(String id) {
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
    public JaspObject creator(String creator) {
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
    public JaspObject created(Date created) {
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
    public JaspObject modified(Date modified) {
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
     * List of tags associated with the object.
     */
    public List<Tag> tags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    /**
     * Associates a tag with the object.
     */
    public JaspObject tag(Tag tag) {
        tags().add(tag);
        return this;
    }

}
