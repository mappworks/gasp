package gasp.app.api;

import com.vividsolutions.jts.geom.Envelope;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Helper class for accessing request query string parameters.
 */
public class RequestParams {

    HttpServletRequest req;
    Map<String,String[]> raw;

    public RequestParams(HttpServletRequest req) {
        this.req = req;
        raw = new LinkedHashMap<>(req.getParameterMap());
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        return Optional.ofNullable(raw.get(key)).map((arr) -> arr[0]).map((Function)parser(clazz));
    }

    public <T> Optional<T> consume(String key, Class<T> clazz) {
        Optional<T> val = get(key, clazz);
        if (val.isPresent()) {
            raw.remove(key);
        }
        return val;
    }

    public <R,S,T> Optional<T> consume(String k1, Class<R> c1, String k2, Class<S> c2,
           BiFunction<Optional<R>,Optional<S>,T> parser) {
        Optional<R> v1 = get(k1, c1);
        Optional<S> v2 = get(k2, c2);

        return Optional.ofNullable(parser.apply(v1, v2));
    }

    public Map<String,Object> all() {
        return (Map) raw;
    }

    <T> Function<String,?> parser(Class<T> clazz) {
        if (clazz == Integer.class) return Integer::parseInt;
        if (clazz == Double.class) return Double::parseDouble;

        if (clazz == Envelope.class) {
            return (str) -> {
                String[] split = str.split("\\s*,\\s*");
                Double[] bbox = Arrays.stream(split).map(Double::parseDouble).toArray(Double[]::new);
                return new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
            };
        }

        return Objects::toString;
    }
}
