package jasp.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import jasp.core.Config;
import jasp.core.util.Passwords;

@Parameters(commandNames="encrypt", commandDescription="Encrypts a password with the jasp master password")
public class EncryptCmd extends BaseCmd {

    @Parameter(names = {"-p", "--password"}, description="Password to encrypt", password = true, required = true)
    String password;

    @Override
    protected void doCommand(JaspCLI cli) throws Exception {
        Config config = new Config();
        cli.console().println(Passwords.encrypt(password, config));
    }
}
