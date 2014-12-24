package jasp.app.security;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Initializes web security.
 * <p>
 * This class will get picked up automatically by servlet containers configured to
 * scan for {@link javax.servlet.ServletContainerInitializer} instances.
 * </p>
 */
@Order(300)
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
}
