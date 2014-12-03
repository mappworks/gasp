package jasp.app;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.webapp.WebAppContext;

import java.nio.file.Paths;
import java.util.Optional;

/**
 * Debug runner for the application.
 */
public class Run {

    public static void main(String[] args) throws Exception {
        Server server = new Server(Optional.ofNullable(Integer.getInteger("jetty.port")).orElse(8000));

        HandlerList handlers = new HandlerList();

        WebAppContext context = new WebAppContext();
        context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
        context.setResourceBase("src/main/webapp/");
        context.setContextPath("/");
        context.setParentLoaderPriority(true);
        handlers.addHandler(context);
        handlers.addHandler(new ShutdownHandler("jasp"));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        server.setHandler(handlers);
        server.start();
        server.join();
    }
}
