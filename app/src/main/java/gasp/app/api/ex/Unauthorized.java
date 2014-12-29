package gasp.app.api.ex;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class Unauthorized extends RuntimeException {

    public Unauthorized() {
    }

    public Unauthorized(String message) {
        super(message);
    }
}
