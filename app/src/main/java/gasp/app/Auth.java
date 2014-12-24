package gasp.app;


import com.google.common.collect.Maps;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping(value = "/app/auth")
public class Auth {

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map get(HttpServletRequest req) {
        // look for a session
        HttpSession session = req.getSession(false);
        if (session == null) {
            // no session, 401
            throw new Unauthorized();
        }

        Map<String,Object> obj = Maps.newLinkedHashMap();
        obj.put("id", session.getId());
        obj.put("created", new Date(session.getCreationTime()));
        obj.put("modified", new Date(session.getLastAccessedTime()));

        return obj;
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public static class Unauthorized extends RuntimeException {
    }
}
