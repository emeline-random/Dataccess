package dataccess.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@EqualsAndHashCode
public class Row {

    private HashMap<String, Object> attributes;
    @Setter
    private List<Object> values;

    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
        this.values = Arrays.asList(attributes.values().toArray());
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public ArrayList<String> getKeys() {
        return new ArrayList<>(attributes.keySet());
    }

    public void convert() {
        int i = 0;
        for (String key : this.attributes.keySet()) {
            this.attributes.replace(key, this.values.get(i));
            i++;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Set<String> keys = this.attributes.keySet();
        for (String key : keys) {
            builder.append(key).append(" : ").append(this.attributes.get(key)).append(" \n");
        }
        return new String(builder);
    }
}
