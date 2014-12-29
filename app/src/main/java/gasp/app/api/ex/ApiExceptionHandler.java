package gasp.app.api.ex;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Global exception handler, returns exceptions as json.
 * <p>
 *  This exception handler dumps out the components of a stack trace as JSON. The
 *  trace is dumped in reverse to provide the most relevant error at the top.
 * </p>
 */
@ControllerAdvice
public class ApiExceptionHandler {

    static Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public @ResponseBody ExceptionReport error(Exception e, HttpServletResponse response) {

        HttpStatus status = status(e);
        response.setStatus(status.value());

        // log at warning if 500, else debug
        if (status.is5xxServerError()) {
            LOG.warn(e.getMessage(), e);
        }
        else {
            LOG.debug(e.getMessage(), e);
        }

        return new ExceptionReport(e);
    }

    HttpStatus status(Exception e) {
        ResponseStatus status = e.getClass().getAnnotation(ResponseStatus.class);
        return status != null ? status.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class ExceptionReport {

        String message;
        List<Map<String,String>> stack = new ArrayList<>();

        public ExceptionReport(Exception cause) {
            List<Throwable> chain = Lists.reverse(Throwables.getCausalChain(cause));
            for (Throwable t : chain) {
                if (message == null && t.getMessage() != null) {
                    message = t.getMessage();
                }

                Map<String,String> link = Maps.newLinkedHashMap();
                link.put("message", t.getMessage());
                link.put("trace", trace(t));
                stack.add(link);
            }
        }

        String trace(Throwable t) {
            StringBuilder trace = new StringBuilder();
            for (StackTraceElement e : t.getStackTrace()) {
                trace.append(e.toString()).append('\n');
            }
            return trace.toString();
        }

    }
}
