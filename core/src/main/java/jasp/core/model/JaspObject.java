package jasp.core.model;

import java.util.Date;

/**
 * Base class for model objects.
 */
public class JaspObject {

    /**
     * object id
     */
    Object id;

    /**
     * date/time of object creation.
     */
    Date created;

    /**
     * date/time of last object modification.
     */
    Date modified;

    /**
     * extensible metadata
     */
    Metadata meta;

    /**
     * The object id.
     */
    public Object id() {
        return id;
    }

        /**
     * Sets the object id.
     * <p>
     * Application code should never call this method.
     * </p>
     */
    public JaspObject id(Object id) {
        this.id = id;
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
}
