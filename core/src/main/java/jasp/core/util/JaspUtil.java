package jasp.core.util;

import org.jasypt.digest.StandardByteDigester;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Various utility routines.
 */
public class JaspUtil {

    public static Optional<String> lookupConfigProperty(String prop, ApplicationContext appContext) {
        // lookup order:
        // 1. web.xml
        // 2. java system property
        // 3. environment variable

        String value = null;

        if (appContext instanceof WebApplicationContext) {
            ServletContext servletContext = ((WebApplicationContext) appContext).getServletContext();
            value = servletContext.getInitParameter(prop);
        }

        if (value == null) {
            value = System.getProperty(prop);
        }

        if (value == null) {
            value = System.getenv(prop);
        }

        return Optional.ofNullable(value);
    }

    public static Optional<Path> lookupConfigFile(String name, ApplicationContext appContext) {
        // lookup JASP_HOME, or fall back to ~/.jasp
        Path home = lookupConfigProperty("JASP_HOME", appContext).map((String s) -> {
            return Paths.get(s);
        }).orElse(Paths.get(System.getProperty("user.home"), ".jasp"));

        // ensure the file exists
        return Optional.of(home.resolve(name)).filter(p -> p.toFile().exists());
    }

    public static StandardPBEStringEncryptor encryptor(String master) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(master);
        encryptor.initialize();
        return encryptor;
    }

    public static StandardByteDigester digester() {
        StandardByteDigester digester = new StandardByteDigester();
        digester.setAlgorithm("SHA-256");
        digester.setIterations(100000);
        digester.setSaltSizeBytes(16);
        digester.initialize();
        return digester;
    }
}
