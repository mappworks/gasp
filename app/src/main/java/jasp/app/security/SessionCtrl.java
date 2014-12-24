package jasp.app.security;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jasp.app.Auth.Unauthorized;
import jasp.core.util.Json;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/auth/session", produces = APPLICATION_JSON_VALUE)
public class SessionCtrl {

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Session get(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            throw new Unauthorized();
        }

        Session s = new Session();
        s.id = session.getId();
        s.created = new Date(session.getCreationTime());
        s.modified = new Date(session.getLastAccessedTime());

        AppUser.get().ifPresent((u) ->s.user = u.getUsername());
        return s;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class Session {
        String id;
        String user;

        @JsonSerialize(using = Json.DateSerializer.class)
        Date created;

        @JsonSerialize(using = Json.DateSerializer.class)
        Date modified;
    }
}
