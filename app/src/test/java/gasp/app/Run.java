package gasp.app;

import com.google.common.base.Throwables;
import gasp.app.security.SecurityInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.web.SpringServletContainerInitializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Debug runner for the application.
 */
public class Run {

    public static void main(String[] args) throws Exception {
        Server server = new Server(Optional.ofNullable(Integer.getInteger("jetty.port")).orElse(8000));

        HandlerList handlers = new HandlerList();

        WebAppContext context = new WebAppContext();
        context.setResourceBase("src/main/webapp/");
        context.setContextPath("/gasp");
        context.setParentLoaderPriority(true);
        context.setWelcomeFiles(new String[]{"app/"});

        // there is an issue with jetty not picking up classes for ServletContainerInitializers that
        // pull in classes from the classpath that are not in a jar, so we do it manually here
        // this doesn't work on jetty 9 unfortunately
        context.addEventListener(new ServletContextListener() {
            @Override
            public void contextInitialized(ServletContextEvent sce) {
                try {
                    Set<Class<?>> initializers = new LinkedHashSet<Class<?>>();
                    initializers.add(Initializer.class);
                    initializers.add(SecurityInitializer.class);

                    new SpringServletContainerInitializer().onStartup(initializers, sce.getServletContext());
                } catch (ServletException e) {
                    throw Throwables.propagate(e);
                }
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
            }
        });
        //context.addEventListener(new AppSessionEventPublisher());
        handlers.addHandler(context);

        server.setHandler(handlers);
        server.start();
        server.join();
    }
}
