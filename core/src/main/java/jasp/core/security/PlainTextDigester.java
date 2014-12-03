package jasp.core.security;

import org.jasypt.digest.ByteDigester;

import java.util.Arrays;

public class PlainTextDigester implements ByteDigester {
    @Override
    public byte[] digest(byte[] message) {
        return message;
    }

    @Override
    public boolean matches(byte[] message, byte[] digest) {
        return Arrays.equals(message, digest);
    }
}
