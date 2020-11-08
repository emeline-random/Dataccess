package dataccess.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
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
            if (column.getName().toUpperCase().equals(columnName.toUpperCase())) return column;
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
            if (key.getName().toLowerCase().equals(name.toLowerCase())) return key;
        }
        return null;
    }

    public boolean isPrimaryKey(Column column) {
        return this.primaryKeys.stream().anyMatch(primaryKey -> primaryKey.getName().equalsIgnoreCase(column.getName()));
    }

    public void print() {
        System.out.println(this.name.toUpperCase() + "\n");
        for (Column column : this.attributes) {
            System.out.println(column.getName() + " " + column.getSqlType() + " null=" + column.isNullable() +
                    " unique=" + column.isUnique() + "\n");
        }
        System.out.println("primary keys : ");
        for (Column pk : this.primaryKeys) System.out.println(pk);
        System.out.println("foreign keys : ");
        for (ForeignKey fk : this.foreignKeys) System.out.println(fk.getName());
    }

    @Override
    public String toString() {
        return name;
    }
}
