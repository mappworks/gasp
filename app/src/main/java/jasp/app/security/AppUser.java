package jasp.app.security;

import jasp.core.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

public class AppUser extends org.springframework.security.core.userdetails.User {

    public static Optional<AppUser> get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Optional.empty();
        }

        if (auth.getPrincipal() instanceof AppUser) {
            return Optional.of(((AppUser) auth.getPrincipal()));
        }

        return Optional.empty();
    }

    User user;

    AppUser(User user) {
        super(user.handle(), new String(user.password()), Collections.emptyList());
        this.user = user;
    }

    public User user() {
        return user;
    }
}
