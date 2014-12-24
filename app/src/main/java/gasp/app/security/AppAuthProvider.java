package gasp.app.security;

import com.google.common.base.Throwables;
import gasp.app.App;
import gasp.app.db.DataSourceProvider;
import gasp.core.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Authentication provider that authenticates by simply connection to the underlying
 * database.
 */
public class AppAuthProvider extends AbstractUserDetailsAuthenticationProvider {

    App app;

    public AppAuthProvider(App app) {
        this.app = app;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authToken)
        throws AuthenticationException {

    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authToken)
        throws AuthenticationException {

        User user = new User()
            .handle(username)
            .password(authToken.getCredentials().toString().getBytes());

        DataSourceProvider dsp = app.dataSourceProvider();
        try {
            DataSource db = dsp.get(user, app);
            try {
                try (Connection cx = db.getConnection()) {
                }
            }
            catch(SQLException e) {
                Throwable root = Throwables.getRootCause(e);
                throw new AuthenticationException(root.getMessage(), e){};
            }
        }
        catch(Exception e) {
            Throwables.propagateIfInstanceOf(e, AuthenticationException.class);
            throw new AuthenticationException("Failed to authenticate user: " + username, e){};
        }

        // scramble the user password
        //user.password(Passwords.scramble(user.password()));
        return new AppUser(user);
    }
}
