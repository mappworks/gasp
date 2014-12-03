package jasp.core.security;


/**
 * Facade for security subsystem.
 */
public class Security {

    SecurityService dao;

    public Security(SecurityService dao) {
        this.dao = dao;
    }

    /**
     * The security data access object.
     */
    public SecurityService dao() {
        return dao;
    }

//    StringEncryptor encryptor;
//    ByteDigester digester;
//
//    public Security(StringEncryptor encryptor, ByteDigester digester) {
//        this.encryptor = encryptor;
//        this.digester = digester;
//    }
//
//    public String encode(String passwd) {
//        return encryptor.encrypt(passwd);
//    }
//
//    public String decode(String encoded) {
//        return encryptor.decrypt(encoded);
//    }
//
//    public byte[] digest(Password passwd) {
//        return digester.digest(passwd.bytes());
//    }
//
//    public boolean check(Password passwd, byte[] digest) {
//        return digester.matches(passwd.bytes(), digest);
//    }
}
