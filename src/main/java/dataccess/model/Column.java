package dataccess.model;

import dataccess.utils.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@EqualsAndHashCode
@Setter @Getter
public class Column {

    private String name;
    private String type;
    private Type.Types sqlType;
    private boolean nullable = true, unique = false;
    private ArrayList<String> checkList = new ArrayList<>();

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
        this.setType(type);
    }

    public Column() {
    }

    public Column(ForeignKey foreignKey) {
        this.setName(foreignKey.getName());
        this.setType(foreignKey.getType());
        this.setNullable(foreignKey.isNullable());
        this.setUnique(foreignKey.isUnique());
        this.setCheckList(foreignKey.getCheckList());
    }

    public void setType(String type) {
        this.type = type;
        if (type != null) {
            String t = type.toLowerCase();
            if (t.contains("char")) this.sqlType = Type.Types.STRING;
            else if (t.contains("date") || t.contains("time")) this.sqlType = Type.Types.DATE;
            else this.sqlType = Type.Types.NUMBER;
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

}
