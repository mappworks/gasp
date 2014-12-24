package jasp.app.api;

import jasp.app.App;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Base class for controller mock tests.
 */
public class CtrlTestBase {

    @Mock
    protected App app;

    protected MockMvc mvc;

    @Before
    public void setUpMocks() {
        MockitoAnnotations.initMocks(this);
    }

    protected void doSetupMVC(Object ctrl) {
        mvc = MockMvcBuilders.standaloneSetup(ctrl).build();
    }
}
