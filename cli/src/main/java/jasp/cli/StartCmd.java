package jasp.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import java.nio.file.Paths;

@Parameters(commandNames="start", commandDescription="Starts the jasp server")
public class StartCmd extends BaseCmd {

    @Parameter(names = {"-p", "--port"}, description="Port to listen on")
    Integer port = 8000;

    @Parameter(names = {"-s", "--stop"}, description="Stop key")
    String stopKey = "jasp";

    @Override
    protected void doCommand(JaspCLI cli) throws Exception {
        Server server = new Server(port);
        HandlerList handlers = new HandlerList();

        WebAppContext context = new WebAppContext();
        context.setWar(Paths.get(System.getProperty("app.home"), "war").toString());

        handlers.addHandler(context);
        handlers.addHandler(new ShutdownHandler(server, stopKey));

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
