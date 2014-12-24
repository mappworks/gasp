package gasp.app.security;

import gasp.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig {

    @Autowired
    public void configure(AuthenticationManagerBuilder auth, App app) throws Exception {
        auth.authenticationProvider(new AppAuthProvider(app));
    }

    /**
     * Security configuration for api that enables HTTP basic auth and
     * ensures no session creation.
     */
    @Configuration
    @Order(1)
    public static class ApiConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.regexMatcher("/api/.+")
                .authorizeRequests()
                    .anyRequest().authenticated().and()
                .csrf().disable()
                .httpBasic().and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER);
        }
    }

    /**
     * Security configuration for frontend app that enables anonymous access
     * app assets like javascript and css. Allows login page to use assets
     * before authentication.
     */
    @Configuration
    @Order(2)
    public static class AppAssetConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.regexMatcher("/app/.+\\.(:?css|js)")
                .anonymous();
        }
    }

    /**
     * Security configuration for frontend app that enables form login and
     * configures sessions.
     */
    @Configuration
    @Order(3)
    public static class AppConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.regexMatcher("/auth/(?:login|logout)")
                .authorizeRequests()
                    .anyRequest().authenticated().and()
                .csrf().disable()
                .formLogin()
                    .loginPage("/app/")
                    .loginProcessingUrl("/auth/login")
                    .defaultSuccessUrl("/app/")
                    .permitAll()
                    .and()
                .logout()
                    .logoutUrl("/auth/logout")
                    .logoutSuccessUrl("/app")
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        }
    }

    @Configuration
    @Order(4)
    public static class SessionConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.regexMatcher("/auth/session")
                .anonymous().and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER);
        }
    }
}
