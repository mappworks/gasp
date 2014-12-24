package gasp.core.util;

import gasp.core.Config;
import org.jasypt.digest.ByteDigester;
import org.jasypt.digest.StandardByteDigester;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.util.Arrays;
import java.util.Optional;

public class Passwords {

    static StandardByteDigester DIGESTER = new StandardByteDigester();
    {
        DIGESTER.setAlgorithm("SHA-256");
    }

    public static StringEncryptor encryptor(String master) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(master);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.initialize();
        return encryptor;
    }

    public static ByteDigester digester() {
        return DIGESTER;
    }

    public static String encrypt(String passwd, Config config) {
        Optional<String> master = config.get(Config.GROUP_SECURITY, "master");
        return master.map((m) -> encryptor(m).encrypt(passwd)).orElse(passwd);
    }

    public static String decrypt(String encrypted, Config config) {
        Optional<String> master = config.get(Config.GROUP_SECURITY, "master");
        return master.map((m) -> encryptor(m).decrypt(encrypted)).orElse(encrypted);
    }

    public static byte[] scramble(byte[] passwd) {
        Arrays.fill(passwd, (byte)0);
        return passwd;
    }
}


