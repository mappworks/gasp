package jasp.core.security;

/**
 * Utility class for working with passwords in a secure way.
 * <p>
 *
 * </p>
 */
public class Password {

    byte[] passwd;

    public Password(byte[] passwd) {
        this.passwd = passwd;
    }

    byte[] bytes() {
        return passwd;
    }

    public void scramble() {

    }
}
