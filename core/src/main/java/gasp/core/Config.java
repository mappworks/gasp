package gasp.core;

import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Core application configuration.
 */
public class Config implements ApplicationContextAware {

    public static final String GROUP_CORE = "core";
    public static final String GROUP_DATABASE = "database";
    public static final String GROUP_SECURITY = "security";

    static Logger LOG = LoggerFactory.getLogger(Config.class);

    @Autowired(required = false)
    Collection<ConfigDelegate> delegates;

    ApplicationContext appContext;

    ConfigFile configFile;

    /**
     * Returns the system property for the specified config path.
     * <p>
     *  The system property is the result of joining all components of the path
     *  with a '.' and prefixing with 'gasp.'. For example:
     *  <pre>
     *    toSysProp('database', 'host') -> 'gasp.database.host'
     *  </pre>
     * </p>
     */
    public static String toSysProp(String... path) {
        return "gasp." + String.join(".", path).toLowerCase();
    }

    /**
     * Returns the environment variable for the specified config path.
     * <p>
     *  The system property is the result of joining all components of the path
     *  with a '_' and prefixing with 'GASP_'. For example:
     *  <pre>
     *    toSysProp('database', 'host') -> 'GASP_DATABASE_HOST'
     *  </pre>
     * </p>
     */
    public static String toEnvVar(String... path) {
        return "GASP_" + String.join("_", path).toUpperCase();
    }

    public Config() {
        configFile = new ConfigFile(Paths.get(System.getProperty("user.home"), ".gasp", "config").toFile());
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = appContext;
    }

    /**
     * Looks up a config value.
     * <p>
     * The lookup algorithm is as follows.
     * <ol>
     *   <li>Look for a value by delegating to instances of {@link Config.ConfigDelegate} in the
     *   app context.</li>
     *   <li>Look for a servlet context parameter</li>
     *   <li>Look for a java system property</li>
     *   <li>Look for an environment variable</li>
     *   <li>Look for a value in ~/.gasp/config ini file</li>
     * </ol>
     * </p>
     * <p>
     *  Config values are identified by a "path". For example: "database", "host". This path is mapped as follows:
     *  <ol>
     *   <li>
     *       Servlet context parameters: Underscore delimited, uppercased, and prefixed. For example:
     *       <tt>(database, host) -> GASP_DATABASE_HOST</tt>
     *   </li>
     *   <li>
     *       Java system properties: Period delimited, lowercased, and prefixed. For example:
     *       <tt>(database, host) -> -Dgasp.database.host</tt>
     *   </li>
     *   <li>
     *       Environment variables: Same as servlet context parameters.
     *   </li>
     *   <li>
     *      Config file: The first n-1 components are mapped to a group named (period delimited) and the last component
     *      of the path is mapped to a key in the section. For example:
     *      <tt>(database, host) -></tt>
     *      <pre>
     *      [database]
     *      host =
     *      </pre>
     *   </li>
     *  </ol>
     *
     * </p>
     * @param path The "path"
     *
     * @return The optional config value.
     */
    public Optional<String> get(String... path) {
        // first check config delegates
        Optional<String> val = Optional.empty();

        if (delegates != null) {
            Iterator<ConfigDelegate> it = delegates.iterator();
            while(it.hasNext() && !val.isPresent()) {
                val = it.next().get(path);
            }
        }

        String var = toEnvVar(path);
        String prop = toSysProp(path);

        // next look in web.xml
        if (!val.isPresent() && appContext instanceof WebApplicationContext) {
            ServletContext servletContext = ((WebApplicationContext) appContext).getServletContext();
            val = Optional.ofNullable(servletContext.getInitParameter(var));
        }

        // next look for system property
        if (!val.isPresent()) {
            val = Optional.ofNullable(System.getProperty(prop));
        }

        // next look for environment variable
        if (!val.isPresent()) {
            val = Optional.ofNullable(System.getenv(var));
        }

        // finally check ~/.gasp/config
        if (!val.isPresent() && path.length > 1) {
            String group = String.join(".", Arrays.copyOfRange(path, 0, path.length-1)).toLowerCase();
            String key = path[path.length-1];

            val = Optional.ofNullable(configFile.get().get(group, key));
        }

        return val;
    }

    public interface ConfigDelegate {
        Optional<String> get(String... path);
    }

    static class ConfigFile {

        File file;
        long modified = -1;

        transient Wini ini;

        ConfigFile(File file) {
            this.file = file;
        }

        Wini get() {
            if (ini == null || file.lastModified() > modified) {
                synchronized (this) {
                    if (ini == null) {
                        try {
                            ini = new Wini(file);
                            modified = file.lastModified();
                            Config.LOG.info(format("Loaded config from: %s", file.getPath()));
                        } catch (IOException e) {
                            ini = new Wini();
                            Config.LOG.warn(format("Error reading config file: %s", e));
                        }
                    }
                }
            }
            return ini;
        }
    }
}
