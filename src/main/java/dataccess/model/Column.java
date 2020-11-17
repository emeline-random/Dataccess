package dataccess.model;

import dataccess.utils.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;

@EqualsAndHashCode
public class Column {

    @Getter
    @Setter
    private String name;
    @Getter
    private String type;
    @Getter
    private Type.Types sqlType;
    @Getter @Setter
    private boolean nullable = true, unique = false;
    @Getter @Setter
    private ArrayList<String> checkList = new ArrayList<>();

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
        this.setType(type);
    }

    public Column() {
    }

    public void setType(String type) {
        this.type = type;
        String t = type.toLowerCase();
        if (t.contains("char")) this.sqlType = Type.Types.STRING;
        else if (t.contains("date") || t.contains("time")) this.sqlType = Type.Types.DATE;
        else this.sqlType = Type.Types.NUMBER;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
