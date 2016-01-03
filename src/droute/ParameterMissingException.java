package droute;

import java.util.NoSuchElementException;

public class ParameterMissingException extends NoSuchElementException {
    public ParameterMissingException(String type, String name) {
        super("Missing required " + type + ": " + name);
    }
}
