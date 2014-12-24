package gasp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.PrintWriter;

public abstract class BaseCmd {

    @Parameter(names={"-h", "--help"}, description="Provides help for this command", help=true)
    boolean help;

    @Parameter(names={"-x", "--debug"}, description="Runs command in debug mode", help=true)
    boolean debug;

    public final void run(GaspCLI cli) throws Exception {
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

    protected abstract void doCommand(GaspCLI cli) throws Exception;

    protected void usage(GaspCLI cli) {
        JCommander jc = new JCommander(this);
        String cmd = this.getClass().getAnnotation(Parameters.class).commandNames()[0];
        jc.setProgramName("gasp " + cmd);
        jc.usage();
    }

    protected void print(Exception e, GaspCLI cli) {
        e.printStackTrace(new PrintWriter(cli.console()));
    }
}
