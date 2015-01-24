package gasp.app;

import gasp.core.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;

@Component
public class JacksonConfig {

    @Autowired
    RequestMappingHandlerAdapter handlerAdapter;

    @PostConstruct
    public void init() {
        for (HttpMessageConverter converter : handlerAdapter.getMessageConverters()) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                configure((MappingJackson2HttpMessageConverter)converter);
            }
        }
    }

    void configure(MappingJackson2HttpMessageConverter converter) {
        converter.setObjectMapper(Json.mapper());
    }
}
