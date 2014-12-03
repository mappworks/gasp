package jasp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import jasp.core.App;
import jasp.core.Bootstrap;
import jasp.core.security.Security;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.io.PrintWriter;

public abstract class BaseCmd {

    @Parameter(names={"-h", "--help"}, description="Provides help for this command", help=true)
    boolean help;

    @Parameter(names={"-x", "--debug"}, description="Runs command in debug mode", help=true)
    boolean debug;

    public final void run(JaspCLI cli) throws Exception {
        if (help) {
            usage(cli);
            return;
        }

        if (debug) {
            //setUpDebugLogging();
        }
        try {
            doCommand(cli);
        }
        catch(Exception e) {
            if (debug) {
                print(e, cli);
            }
            else {
                cli.console().println(e.getMessage());
            }
        }
        cli.console().flush();
    }

    protected abstract void doCommand(JaspCLI cli) throws Exception;

    protected void usage(JaspCLI cli) {
        JCommander jc = new JCommander(this);
        String cmd = this.getClass().getAnnotation(Parameters.class).commandNames()[0];
        jc.setProgramName("jasp " + cmd);
        jc.usage();
    }

    protected void print(Exception e, JaspCLI cli) {
        e.printStackTrace(new PrintWriter(cli.console()));
    }

    protected App loadApp() {
        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext("classpath:/appContext.xml");
        appContext.refresh();

        return appContext.getBean(App.class);
    }

//    protected Security loadSecurity() {
//        Bootstrap boot = new Bootstrap();
//        boot.setApplicationContext(new GenericApplicationContext());
//
//        App app = boot.app();
//        return boot.security(app);
//    }
}
