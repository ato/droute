package org.meshy.leanhttp;

import java.util.Collections;
import java.util.List;

public class MultiParameter extends AbstractParameter {
    private final List<String> values;

    public MultiParameter(String type, String name, List<String> values) {
        super(type, name);
        if (values == null) {
            this.values = Collections.emptyList();
        } else {
            this.values = values;
        }
    }

    public String orElse(String other) {
        if (values.size() > 1) {
            throw new ParameterAmbiguousException(type, name);
        }
        return values.isEmpty() ? other : values.get(0);
    }

    public List<String> asList() {
        return values;
    }
}
