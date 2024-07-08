package cat.udl.eps.softarch.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Authorization required")
public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException() {
        super("Authorization required");
    }
}