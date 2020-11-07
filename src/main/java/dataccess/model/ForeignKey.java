package dataccess.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @EqualsAndHashCode(callSuper = false)
public class ForeignKey extends Column{

    private String referencedColumnName;
    private String referencedTableName;
    private String name;

    public ForeignKey() {
        super();
    }

    public ForeignKey(String columnName, String referencedTableName, String  referencedColumnName) {
        this.setName(columnName);
        this.referencedColumnName = referencedColumnName;
        this.referencedTableName = referencedTableName;
    }

    public String toString() {
        return this.getName();
    }
}
