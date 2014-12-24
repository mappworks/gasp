package gasp.app.test;

import gasp.app.App;
import gasp.app.Bootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Base class for tests that require an entirely configured
 * application to run.
 */
public class SystemTestBase {

    static AnnotationConfigApplicationContext appContext;

    @BeforeClass
    public static void loadApp() {
        appContext = new AnnotationConfigApplicationContext();
        appContext.register(Bootstrap.class);
        appContext.refresh();
    }

    @AfterClass
    public static void destroyApp() {
        appContext.destroy();
    }

    protected App app() {
        return appContext.getBean(App.class);
    }
}
