package gasp.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Controller that serves up the front end client application.
 */
@Controller
public class Frontend {

    static final String APP_PREFIX = "/app";
    static final Pattern APP_PREFIX_REGEX = Pattern.compile(format("^%s", APP_PREFIX));

    ResourceHttpRequestHandler handler;

    @Autowired
    public Frontend(App app) {
        handler = new ResourceHttpRequestHandler();
        handler.setApplicationContext(app.context());
        handler.setLocations(Arrays.asList(new ServletContextResource(app.servletContext(), "/client/")));
    }

    @RequestMapping(value = APP_PREFIX + "/**")
    public void get(HttpServletRequest req, HttpServletResponse rsp) throws Exception {
        String path = req.getPathInfo();
        if (path == null) {
            path = req.getServletPath();
        }

        // handle index
        if (path.endsWith("/")) {
            // index.html
            path += "index.html";
        }

        // strip off the app prefix
        path = APP_PREFIX_REGEX.matcher(path).replaceFirst("");
        if (path.isEmpty()) {
            // redirect
            rsp.sendRedirect(format("%s%s/", req.getServletContext().getServletContextName(), APP_PREFIX));
            return;
        }

        // pass off to handler, have to set attribute before doing so, a hack to simulate going
        // through the spring dispatcher
        req.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, path);
        handler.handleRequest(req, rsp);
    }
}
