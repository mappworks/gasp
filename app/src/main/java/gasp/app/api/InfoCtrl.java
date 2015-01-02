package gasp.app.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gasp.app.App;
import gasp.core.util.Json.DateSerializer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/api/info")
public class InfoCtrl extends BaseCtrl {

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Info get() throws Exception {
        return new Info(app);
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    static class Info {
        final String version;
        final String revision;

        @JsonSerialize(using = DateSerializer.class)
        final Date timestamp;

        Info(App app) {
            version = app.version();
            revision = app.revision();
            timestamp = app.timestamp();
        }
    }
}
