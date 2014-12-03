package jasp.core.security;

import jasp.core.Config;
import org.jasypt.digest.StringDigester;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.util.Optional;

public class Passwords {

    public static StringEncryptor encryptor(String master) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(master);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.initialize();
        return encryptor;
    }

    public static String encrypt(String passwd, Config config) {
        Optional<String> master = config.get(Config.GROUP_SECURITY, "master");
        return master.map((m) -> encryptor(m).encrypt(passwd)).orElse(passwd);
    }

    public static String decrypt(String encrypted, Config config) {
        Optional<String> master = config.get(Config.GROUP_SECURITY, "master");
        return master.map((m) -> encryptor(m).decrypt(encrypted)).orElse(encrypted);
    }
}


