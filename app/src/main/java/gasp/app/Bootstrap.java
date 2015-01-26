package gasp.app;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import gasp.app.App.State;
import gasp.app.db.CachingDataSourceProvider;
import gasp.app.db.DataSourceProvider;
import gasp.app.db.DefaultDataSourceProvider;
import gasp.core.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Bootstrap configuration for the application.
 */
@Configuration
@ComponentScan(basePackages = "gasp.app")
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

    @Bean(name = "metrics")
    public MetricRegistry metrics() {
        // create a new metric registry
        MetricRegistry metrics = new MetricRegistry();

        // enable jmx reporting
        JmxReporter.forRegistry(metrics).build().start();

        return metrics;
    }

    @Bean(name = "dataSourceProvider")
    public DataSourceProvider dataSourceProvider(App app) {
        DataSourceProvider dsProvider =
            loadFactory("dataSourceProvider", DataSourceProvider.class, new DefaultDataSourceProvider(), app.config());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading data source provider: " + dsProvider.getClass().getName());
        }

        app.dataSourceProvider(new CachingDataSourceProvider(dsProvider));
        return dsProvider;
    }

//    @Bean(name = "catalog")
//    public Catalog catalog(DataSource dataSource, Config config, App app) {
//        if (dataSource == null) {
//            // error connecting
//            return null;
//        }
//
//        Catalog cat = new Catalog(dataSource, config);
//        try {
//            cat.init();
//            app.catalog(cat);
//            return cat;
//        }
//        catch(Exception e) {
//            LOG.warn(String.format("Error loading catalog: %s", Throwables.getRootCause(e).getMessage()));
//            LOG.debug("Error loading catalog", e);
//            app.state(State.ERROR).error(e);
//        }
//        return null;
//    }
//
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

        return fallback;

//        // next check for an instance in the app context, otherwise fall back to default
//        try {
//            return appContext.getBean(clazz);
//        }
//        catch(NoSuchBeanDefinitionException e) {
//            return fallback;
//        }
    }
}
