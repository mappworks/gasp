package gasp.core;

import gasp.core.util.Json;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

public class Gasp {

    /**
     * meta properties about app
     */
    static Properties META = new Properties();
    static {
        try {
            META.load(Gasp.class.getResourceAsStream("gasp.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String version() {
        return meta("version").orElse("Latest");
    }

    public static String revision() {
        return meta("revision").orElse("HEAD");
    }

    public static Date timestamp() {
        return meta("timestamp").map((d) -> {
            try {
                return new SimpleDateFormat(Json.DATE_FORMAT).parse(d);
            } catch (ParseException e) {
                throw new RuntimeException("Error parsing app build timestamp", e);
            }
        }).orElse(new Date());
    }

    static Optional<String> meta(String key) {
        String val = META.getProperty(key);
        if (val == null || val.startsWith("@")) {
            return Optional.empty();
        }

        return Optional.of(val);
    }
}
