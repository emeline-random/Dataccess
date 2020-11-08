package dataccess.service;

import dataccess.dao.DaoAccessException;
import dataccess.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public interface QueryService {

    default void completeTable(Table table) throws DaoAccessException { //TODO compléter les colonnes pour savoir si c'est nullable/unique
        ArrayList<Column> columns = this.getAttributes(table);
        ArrayList<Column> pk = new ArrayList<>();
        for (String key : this.getPrimaryKey(table)) {
            for (Column c : columns) {
                if (c.getName().equalsIgnoreCase(key)) {
                    pk.add(new Column(key, c.getType()));
                    break;
                }
            }
            columns.removeIf(column -> column.getName().toUpperCase().equals(key.toUpperCase()));
        }
        ArrayList<ForeignKey> fk = this.getForeignKeys(table);
        for (ForeignKey foreignKey : fk) {
            for (Column c : columns) {
                if (c.getName().equalsIgnoreCase(foreignKey.getName())) {
                    foreignKey.setType(c.getType());
                    break;
                }
            }
            columns.removeIf(column -> column.getName().equalsIgnoreCase(foreignKey.getName()));
        }
        columns.removeIf(column -> fk.stream().anyMatch(foreignKey ->
                foreignKey.getName().toUpperCase().equals(column.getName().toUpperCase())));
        table.setForeignKeys(fk);
        table.setAttributes(columns);
        table.setPrimaryKeys(pk);
        table.setRows(this.getRows(table));
    }

    ArrayList<Database> getDatabases() throws DaoAccessException;

    ArrayList<Table> getTables(Database database) throws DaoAccessException;

    ArrayList<Row> getRows(Table table) throws DaoAccessException;

    default ArrayList<Row> getRows(ResultSet res) throws DaoAccessException {
        ArrayList<Row> rows = new ArrayList<>();
        try {
            while (res.next()) {
                Row row = new Row();
                HashMap<String, Object> attributes = new HashMap<>();
                for (int i = 1; i <= res.getMetaData().getColumnCount(); i++) {
                    attributes.put(res.getMetaData().getColumnName(i), res.getString(i));
                }
                row.setAttributes(attributes);
                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoAccessException(e);
        }
        return rows;
    }

    ArrayList<Column> getAttributes(Table table) throws DaoAccessException;

    default ArrayList<Column> getAttributes(ResultSet result) {
        ArrayList<Column> attributes = new ArrayList<>();
        try {
            while (result.next()) {
                attributes.add(new Column(result.getString("column_name"), result.getString("data_type")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return attributes;
    }

    ArrayList<ForeignKey> getForeignKeys(Table table) throws DaoAccessException;

    ArrayList<String> getPrimaryKey(Table table) throws DaoAccessException;

    Row getParentRow(ForeignKey foreignKey, Row row) throws DaoAccessException;

    default Row getParentRow(ResultSet res) throws DaoAccessException {
        Row parent = new Row();
        try {
            res.next();
            HashMap<String, Object> attributes = new HashMap<>();
            for (int i = 1; i <= res.getMetaData().getColumnCount(); i++) {
                attributes.put(res.getMetaData().getColumnName(i), res.getString(i));
            }
            parent.setAttributes(attributes);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoAccessException(e);
        }
        return parent;
    }

    void truncateTable(Table table) throws DaoAccessException;

    void addTable(Table table) throws DaoAccessException;

    void dropTable(Table table) throws DaoAccessException;

    void addRow (Table table, Row row) throws DaoAccessException;

    Table execute (String query) throws DaoAccessException, SQLException;

    void simpleExecution(String query) throws DaoAccessException;

    void delete(Table table, Row row) throws DaoAccessException;

}
