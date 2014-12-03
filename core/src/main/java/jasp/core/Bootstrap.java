package jasp.core;

import com.google.common.base.Throwables;
import jasp.core.App.State;
import jasp.core.data.DataSourceFactory;
import jasp.core.data.DefaultDataSourceFactory;
import jasp.core.security.DefaultSecurityService;
import jasp.core.security.Security;
import jasp.core.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Bootstraps the application.
 * <p>
 *  This class initializes the security and config subsystems.
 * </p>
 */
@Configuration
public class Bootstrap implements ApplicationContextAware {

    static Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

    ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = appContext;
    }

    @Bean(name = "config")
    public Config config() {
        return new Config();
    }

    @Bean(name = "app")
    public App app(Config config) {
        return new App().state(State.STARTING).context(appContext).config(config);
    }

    @Bean(name = "dataSource")
    public DataSource dataSource(App app) {
        DataSourceFactory dsFactory =
            loadFactory("dataSourceFactory", DataSourceFactory.class, new DefaultDataSourceFactory(), app.config());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading data source with factory: " + dsFactory.getClass().getName());
        }

        try {
            DataSource dataSource = dsFactory.create(app);
            app.dataSource(dataSource);
            return dataSource;
        }
        catch(Exception e) {
            LOG.warn(String.format("Error loading data source: %s", Throwables.getRootCause(e).getMessage()));
            LOG.debug("Error loading data source", e);
            app.state(State.ERROR).error(e);
        }

        return null;
    }

    @Bean(name = "security")
    public Security security(App app, DataSource dataSource) {
        SecurityService secDao =
            loadFactory("securityDAO", SecurityService.class, new DefaultSecurityService(dataSource), app.config());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded security dao: " + secDao.getClass().getName());
        }

        try {
            secDao.init();

            Security sec = new Security(secDao);
            app.security(sec);
            return sec;
        }
        catch(Exception e) {
            LOG.warn(String.format("Error initializing security %s", Throwables.getRootCause(e).getMessage()));
            LOG.debug("Error initializing security", e);
            app.state(State.ERROR).error(e);
        }

        return null;
    }

    <T> T loadFactory(String property, Class<T> clazz, T fallback, Config config) {
        // first check the specified config property
        Optional<String> factoryClass = config.get(Config.GROUP_CORE, property);
        if (factoryClass.isPresent()) {
            return factoryClass.map(className -> {
                try {
                    return (T) Class.forName(className).newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Unable to load factory class: " + clazz, e);
                }
            }).get();
        }

        // next check for an instance in the app context, otherwise fall back to default
        try {
            return appContext.getBean(clazz);
        }
        catch(NoSuchBeanDefinitionException e) {
            return fallback;
        }
    }
}
