package jasp.core.model;

/**
 * Model object with a name, title, and description.
 */
public class NamedObject extends JaspObject {

    /**
     * The object name.
     */
    String name;

    /**
     * The object title.
     */
    String title;

    /**
     * The object description.
     */
    String description;

    /**
     * The object name.
     */
    public String name() {
        return name;
    }

    /**
     * Sets the object name.
     *
     * @param name The name.
     *
     * @return This object.
     */
    public NamedObject name(String name) {
        this.name = name;
        return this;
    }

    /**
     * The object title.
     */
    public String title() {
        return title;
    }

    /**
     * Sets the object title.
     *
     * @param title The title.
     *
     * @return This object.
     */
    public NamedObject title(String title) {
        this.title = title;
        return this;
    }

    /**
     * The object description.
     */
    public String description() {
        return description;
    }

    /**
     * Sets the object description.
     *
     * @param description The description.
     *
     * @return This object.
     */
    public NamedObject description(String description) {
        this.description = description;
        return this;
    }
}
