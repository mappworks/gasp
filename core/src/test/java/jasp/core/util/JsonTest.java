package jasp.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JsonTest {

    @Test
    public void testToFromDate() throws Exception {
        ObjectMapper mapper = Json.mapper();
        mapper.registerModule(
            new SimpleModule()
                .addSerializer(new Json.DateSerializer())
                .addDeserializer(Date.class, new Json.DateDeserializer()));

        Date d1 = new Date();
        StringWriter out = new StringWriter();
        mapper.writeValue(out, d1);

        Map<String,Object> obj = new ObjectMapper().readValue(out.toString().getBytes(), Map.class);
        assertNotNull(obj);
        assertTrue(obj.containsKey("timestamp"));
        assertTrue(obj.containsKey("readable"));

        Date d2 = mapper.readValue(out.toString().getBytes(), Date.class);
        assertEquals(local(d1), local(d2));
    }

    LocalDateTime local(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.SECONDS);
    }

}
