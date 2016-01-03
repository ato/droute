package droute;

public class Parameter extends AbstractParameter {

    private final String value;

    public Parameter(String type, String name, String value) {
        super(type, name);
        this.value = value;
    }

    @Override
    public String orElse(String other) {
        return value == null ? other : value;
    }
}
