package dataccess.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Table {

    private ArrayList<Row> rows;
    private String name;
    private ArrayList<Column> attributes;
    private ArrayList<ForeignKey> foreignKeys;
    private List<Column> primaryKeys; //TODO récupérer les types de chaque colonne
    private Database database;

    public Table(String name) {
        this.name = name;
    }

    public Table(String name, Database database) {
        this.database = database;
        this.name = name;
    }

    public Column getColumn(String columnName) {
        for (Column column : this.attributes)
            if (column.getName().equalsIgnoreCase(columnName)) return column;
        return null;
    }

    public void addColumn(Column column) {
        this.attributes.add(column);
    }

    public void addPrimaryKey(Column key) {
        this.primaryKeys.add(key);
    }

    public void addForeignKey(ForeignKey foreignKey) {
        this.foreignKeys.add(foreignKey);
    }

    public void removeForeignKey(ForeignKey foreignKey) {
        this.foreignKeys.remove(foreignKey);
    }

    public void removePrimaryKey(Column primaryKey) {
        this.primaryKeys.remove(primaryKey);
    }

    public void removeColumn(Column column) {
        this.attributes.remove(column);
    }

    public ForeignKey getForeignKey(String name) {
        for (ForeignKey key : this.foreignKeys) {
            if (key.getName().equalsIgnoreCase(name)) return key;
        }
        return null;
    }

    public boolean isPrimaryKey(Column column) {
        return this.primaryKeys.stream().anyMatch(primaryKey -> primaryKey.getName().equalsIgnoreCase(column.getName()));
    }

    public String print() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.name.toUpperCase()).append("<br/><br/>");
        for (Column column : this.attributes) {
            builder.append(column.getName()).append(" ").append(column.getSqlType()).append(" null=")
                    .append(column.isNullable()).append(" unique=").append(column.isUnique()).append("<br/><br/>");
        }
        builder.append("primary keys : <br/>");
        for (Column pk : this.primaryKeys) builder.append(pk).append("<br/>");
        builder.append("foreign keys : ").append("<br/>");
        for (ForeignKey fk : this.foreignKeys) builder.append(fk.getName()).append("<br/>");
        return builder.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
