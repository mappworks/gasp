package gasp.app;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Enables WebMvc usage in the application.
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/app", "/app/");
        //registry.addRedirectViewController("/app/login", "/app/login/");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // static resource handlers for the client side app
        //registry.addResourceHandler("/app/**").addResourceLocations("/client/app/");
    }
}
