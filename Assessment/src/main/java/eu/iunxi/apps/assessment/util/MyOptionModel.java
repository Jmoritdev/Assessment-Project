package eu.iunxi.apps.assessment.util;

import java.util.Map;
import java.util.TreeMap;
import org.apache.tapestry5.internal.OptionModelImpl;

public class MyOptionModel extends OptionModelImpl {

    private boolean disabled;
    private Map<String, String> attributes = new TreeMap<String, String>();

    public MyOptionModel(String label, Object value, boolean disabled) {
        super(label, value);
        this.disabled = disabled;
    }

    public MyOptionModel(String label, Object value, boolean disabled, String attribute) {
        super(label, value);
        this.disabled = disabled;
        this.attributes.put("class", attribute);
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
