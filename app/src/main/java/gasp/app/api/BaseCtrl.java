package gasp.app.api;

import gasp.app.App;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for api controllers.
 */
public class BaseCtrl {

    @Autowired
    protected App app;
}
