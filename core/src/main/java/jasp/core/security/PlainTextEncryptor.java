package jasp.core.security;

import org.jasypt.encryption.StringEncryptor;

public class PlainTextEncryptor implements StringEncryptor {

    @Override
    public String encrypt(String message) {
        return message;
    }

    @Override
    public String decrypt(String encryptedMessage) {
        return encryptedMessage;
    }
}
