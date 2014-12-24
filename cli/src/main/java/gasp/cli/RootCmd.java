package gasp.cli;


import com.beust.jcommander.Parameter;

public class RootCmd extends BaseCmd {

    @Parameter(names={"-v", "--version"}, description="Prints version info", help=true)
    boolean version;

    @Override
    protected void doCommand(GaspCLI cli) throws Exception {
        if (version) {
            //JEO.printVersionInfo(cli.stream());
        }
    }

    @Override
    protected void usage(GaspCLI cli) {
        cli.usage();
    }
}
