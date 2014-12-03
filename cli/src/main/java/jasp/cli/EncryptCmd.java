package jasp.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import jasp.core.security.Security;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Parameters(commandNames="encrypt", commandDescription="Encrypts a password with the jasp master password")
public class EncryptCmd extends BaseCmd {

    @Parameter(names = {"-p", "--password"}, description="Password to encrypt", password = true, required = true)
    String password;

    @Override
    protected void doCommand(JaspCLI cli) throws Exception {
//        Security sec = loadSecurity();
//        cli.console().println(sec.encode(password));
    }
}
