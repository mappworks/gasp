package gasp.app.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Iterators;
import gasp.core.catalog.Catalog;
import gasp.core.catalog.CatalogQuery;
import gasp.core.model.Dataset;
import gasp.core.util.Json;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DatasetCtrlTest extends CtrlTestBase {

    @InjectMocks
    DatasetCtrl ctrl;

    @Before
    public void setUp() {
        doSetupMVC(ctrl);
    }

    @Test
    public void testList() throws Exception {
        Catalog cat = mock(Catalog.class);
        when(app.catalog()).thenReturn(cat);

        when(cat.datasets(isA(CatalogQuery.class))).thenAnswer((i) ->
            Iterators.forArray(
                new Dataset().query("select * from foo").name("foo").id("1"),
                new Dataset().query("select * from bar").name("bar").id("2"))
        );

        MvcResult result = mvc.perform(get("/api/datasets"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        List<Dataset> l = Json.from(result.getResponse().getContentAsString(),
                new TypeReference<List<Dataset>>() {}).get();
        assertNotNull(l);
        assertEquals(2, l.size());

        l.stream().forEach((d) -> assertTrue(d instanceof Dataset));
        l.stream().anyMatch((d) -> d.name().equals("foo"));
        l.stream().anyMatch((d) -> d.name().equals("bar"));
    }
}
