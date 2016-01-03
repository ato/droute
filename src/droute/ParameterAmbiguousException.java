package droute;

public class ParameterAmbiguousException extends RuntimeException {
    public ParameterAmbiguousException(String type, String name) {
        super(type + " '" + name +"' has multiple values");
    }
}
