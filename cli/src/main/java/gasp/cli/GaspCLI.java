package gasp.cli;

import com.beust.jcommander.JCommander;
//import jline.console.ConsoleReader;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Set;

public class GaspCLI {

    //ConsoleReader console;
    PrintStream console;
    JCommander cmdr;
    RootCmd root;

    public static void main(String[] args) throws Exception {
        GaspCLI cli = new GaspCLI(System.out);
        cli.handle(args);
    }

//    static ConsoleReader createConsoleReader() {
//        try {
//            ConsoleReader reader = new ConsoleReader(System.in, System.out);
//            // needed for CTRL+C not to let the console broken
//            reader.getTerminal().setEchoEnabled(true);
//            return reader;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    public GaspCLI(PrintStream console) {
        this.console = console;
        this.cmdr = initJCommander();
    }

    public PrintStream console() {
        return console;
    }

    void handle(String... args) throws Exception {
        if (args == null || args.length == 0) {
            usage();
            return;
        }

        try {
            cmdr.parse(args);

            JCommander subcmdr = cmdr.getCommands().get(cmdr.getParsedCommand());

            BaseCmd cmd = subcmdr != null ? (BaseCmd) subcmdr.getObjects().get(0) : root;
            cmd.run(this);
        }
        catch(Exception e) {
            if (e.getMessage() != null) {
                console.println(e.getMessage());
            }
            console.flush();
        }
    }

    void usage() {
        Set<String> commands = cmdr.getCommands().keySet();
        Optional<Integer> maxLength = commands.stream().max((s1,s2) -> {
           return Integer.valueOf(s1.length()).compareTo(Integer.valueOf(s2.length()));
        }).map(s -> s.length());

//        try {
            console.println("usage: gasp <command> [<args>]");
            console.println();
            console.println("Commands:");
            console.println();
            for (String cmd : commands) {
                console.print("\t");
                console.print(String.format("%1$-" + maxLength.get() + "s", cmd));
                console.print("\t");
                console.println(cmdr.getCommandDescription(cmd));
            }
            console.println();
            console.println("For detailed help on a specific command use gasp <command> -h");
            console.flush();
//        }
//        catch(IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    JCommander initJCommander() {
        root = new RootCmd();
        JCommander jcmdr = new JCommander(root);
        //jcmdr.addConverterFactory(new JeoCLIConverterFactory());
        jcmdr.addCommand("start", new StartCmd());
        jcmdr.addCommand("encrypt", new EncryptCmd());
        return jcmdr;
    }

}
