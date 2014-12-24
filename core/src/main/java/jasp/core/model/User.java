package jasp.core.model;

/**
 * A user of the system.
 */
public class User extends JaspObject {

    /**
     * User handle.
     */
    String handle;

    /**
     * User password.
     */
    byte[] password;

    /**
     * Users full name.
     */
    String fullName;

    /**
     * The username/login/nick of the user.
     */
    public String handle() {
        return handle;
    }

    /**
     * Sets the user password.
     * @return This object.
     */
    public User password(byte[] password) {
        this.password = password;
        return this;
    }

    /**
     * The user password.
     */
    public byte[] password() {
        return password;
    }

    /**
     * Sets the username/login/nick of the user.
     *
     * @return This object.
     */
    public User handle(String handle) {
        this.handle = handle;
        return this;
    }

    /**
     * The full name of the user.
     */
    public String fullName() {
        return fullName;
    }

    /**
     * Sets the full name of the user.
     *
     * @return This object.
     */
    public User fullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

}
