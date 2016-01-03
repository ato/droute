package droute;

import java.util.Locale;

abstract class AbstractParameter {
    protected final String type;
    protected final String name;

    public AbstractParameter(String type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Return the value of this parameter if present, otherwise return defaultValue.
     *
     * @param defaultValue the value to be returned if this parameter is not present
     * @return the non-null string value otherwise defaultValue
     * @throws ParameterAmbiguousException if this parameter is present more than once
     */
    public abstract String orElse(String defaultValue);

    /**
     * Return the value of this parameter.
     *
     * @return the non-null string value of the parameter
     * @throws ParameterMissingException if this parameter is not present
     * @throws ParameterAmbiguousException if the parameter has more than one value
     */
    public String get() {
        String value = orElse(null);
        if (value == null) {
            throw new ParameterMissingException(type, name);
        }
        return value;
    }

    /**
     * Interpret the value of this parameter as a boolean.
     *
     * @return true if the value case-insensitively matches "true", "on", "yes", "y" or "1". false if the value
     * case-insensitively matches "false", "off", "no", "n" or "0".
     * @throws ParameterFormatException if the value is not a boolean
     * @throws ParameterMissingException if the parameter is not present
     * @throws ParameterAmbiguousException if the parameter has more than one value
     */
    public boolean asBoolean() {
        return parseBoolean(get());
    }

    /**
     * Interprets the value of this parameter as a boolean.
     *
     * @param defaultValue the value to be returned if this parameter is not present
     * @return true if the value case-insensitively matches "true", "on", "yes", "y" or "1". false if the value
     * matches "false", "off", "no", "n" or "0". defaultValue if the parameter is missing or an empty string.
     * @throws ParameterFormatException if the value is not a boolean
     * @throws ParameterAmbiguousException if the parameter has more than one value
     */
    public boolean asBooleanOrDefault(boolean defaultValue) {
        String value = orElse(null);
        return value == null || value.isEmpty() ? defaultValue : parseBoolean(value);
    }

    private boolean parseBoolean(String value) {
        switch (value.toLowerCase(Locale.US)) {
            case "true":
            case "on":
            case "yes":
            case "y":
            case "1":
                return true;
            case "false":
            case "off":
            case "no":
            case "n":
            case "0":
                return false;
            default:
                throw new ParameterFormatException(name);
        }
    }

    /**
     * Interpret the value of this parameter as an int.
     *
     * @return the int value of this parameter
     * @throws ParameterFormatException if the value is not a number
     * @throws ParameterMissingException if the parameter is not present
     * @throws ParameterAmbiguousException if the parameter has more than one value
     */
    public int asInt() {
        try {
            return Integer.parseInt(get());
        } catch (NumberFormatException e) {
            throw new ParameterFormatException(name, e);
        }
    }

    /**
     * Interpret the value of this parameter as a long.
     *
     * @return the int value of this parameter
     * @throws ParameterFormatException if the value is not a number
     * @throws ParameterMissingException if the parameter is not present
     * @throws ParameterAmbiguousException if the parameter has more than one value
     */
    public long asLong() {
        try {
            return Long.parseLong(get());
        } catch (NumberFormatException e) {
            throw new ParameterFormatException(name, e);
        }
    }

    /**
     * Interpret the value of this parameter as a double.
     *
     * @return the int value of this parameter
     * @throws ParameterFormatException if the value is not a number
     * @throws ParameterMissingException if the parameter is not present
     * @throws ParameterAmbiguousException if the parameter has more than one value
     */
    public double asDouble() {
        try {
            return Double.parseDouble(get());
        } catch (NumberFormatException e) {
            throw new ParameterFormatException(name, e);
        }

    }

    /**
     * Interpret the value of this parameter as an Integer.
     *
     * @return the integer value of this parameter or null if it is missing or blank
     * @throws ParameterFormatException if the value is not a number
     * @throws ParameterAmbiguousException if the parameter has more than one value
     */
    public Integer asIntOrNull() {
        try {
            String value = orElse(null);
            return value == null || value.isEmpty() ? null : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ParameterFormatException(name, e);
        }
    }

    /**
     * Interpret the value of this parameter as a Long.
     *
     * @return the long value of this parameter or null if it is missing or blank
     * @throws ParameterFormatException if the value is not a number
     * @throws ParameterAmbiguousException if the parameter has more than one value
     */
    public Long asLongOrNull() {
        try {
            String value = orElse(null);
            return value == null || value.isEmpty() ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ParameterFormatException(name, e);
        }
    }

    /**
     * Interpret the value of this parameter as a Double.
     *
     * @return the double value of this parameter or null if it is missing or blank
     * @throws ParameterFormatException if the value is not a number
     * @throws ParameterAmbiguousException if the parameter has more than one value
     */
    public Double asDoubleOrNull() {
        try {
            String value = orElse(null);
            return value == null || value.isEmpty() ? null : Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw  new ParameterFormatException(name, e);
        }
    }
}
