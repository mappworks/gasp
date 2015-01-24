package gasp.core.util;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.ocpsoft.pretty.time.PrettyTime;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * JSON utility class.
 */
public class Json {

    /**
     * Pretty time instance for human readable dates.
     */
    public static final PrettyTime PRETTY_TIME = new PrettyTime();

    /**
     * ISO-8601 datetime format.
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    /**
     * The Jasckson object mapper used to serialize / deserialize objects.
     */
    public static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JtsModule());
        return mapper;
    }

    /**
     * Serializes an object to a JSON string.
     *
     * @param obj The object to serialize.
     *
     * @return The JSON string.
     */
    public static final String to(Object obj) throws Exception {
        StringWriter out = new StringWriter();
        mapper().writeValue(out, obj);
        return out.toString();
    }

    /**
     * Deserializes an object from a JSON string.
     *
     * @param json The JSON string.
     * @param type The class of object to deserialize.
     *
     * @return The deserialized object.
     */
    public static final <T> Optional<T> from(String json, Class<T> type) throws Exception {
        if (json != null) {
            return Optional.of(mapper().readValue(json, type));
        }
        return Optional.empty();
    }

    /**
     * Deserializes an object from a JSON string.
     *
     * @param json The JSON string.
     * @param type The type of object to deserialize.
     *
     * @return The deserialized object.
     */
    public static final <T> Optional<T> from(String json, TypeReference<T> type) throws Exception {
        if (json != null) {
            return Optional.of(mapper().readValue(json, type));
        }
        return Optional.empty();
    }

    /**
     * Custom Jackson date serializer that serializes dates as a timestamp and human
     * readable combo.
     */
    public static class DateSerializer extends StdSerializer<Date> {
        public DateSerializer() {
            super(Date.class);
        }

        @Override
        public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeStartObject();
            jgen.writeObjectField("timestamp", new SimpleDateFormat(DATE_FORMAT).format(value));
            jgen.writeObjectField("readable", PRETTY_TIME.format(value));
            jgen.writeEndObject();
        }
    }

    /**
     * Custom Jackson date deserializer, counterpart of {@link Json.DateSerializer}.
     */
    public static class DateDeserializer extends StdDeserializer<Date> {

        public DateDeserializer() {
            super(Date.class);
        }

        @Override
        public Date deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
            Date date = null;

            if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                JsonToken t;
                while ((t = jp.nextToken()) != JsonToken.END_OBJECT) {
                    if (t == JsonToken.FIELD_NAME && "timestamp".equals(jp.getCurrentName())) {
                        jp.nextToken();

                        try {
                            date = new SimpleDateFormat(DATE_FORMAT).parse(jp.getValueAsString());
                        } catch (ParseException e) {
                            throw new IOException(e);
                        }
                    }
                }

            }
            return date;

        }
    }
}
