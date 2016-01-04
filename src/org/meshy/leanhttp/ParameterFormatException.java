package org.meshy.leanhttp;

public class ParameterFormatException extends RuntimeException {
    public ParameterFormatException(String name, NumberFormatException e) {
        super("For parameter: " + name, e);
    }

    public ParameterFormatException(String name) {
        this(name, null);
    }
}
