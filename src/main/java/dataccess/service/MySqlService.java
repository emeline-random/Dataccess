package dataccess.service;

import dataccess.dao.DaoAccessException;
import dataccess.dao.DatabaseAccess;
import dataccess.model.*;
import dataccess.utils.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Primary
public class MySqlService implements QueryService {

    private DatabaseAccess access;

    @Override
    public ArrayList<Database> getDatabases() throws DaoAccessException {
        ResultSet res = this.access.execute("SELECT schema_name\n" +
                "FROM information_schema.schemata");
        ArrayList<Database> databases = new ArrayList<>();
        try {
            while (res.next()) {
                databases.add(new Database(res.getString("schema_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.access.closeConnection();
        }
        return databases;
    }

    @Override
    public ArrayList<Table> getTables(Database database) throws DaoAccessException {
        this.access.changeMysqlDatabase(database.getName());
        ResultSet res = this.access.execute("SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = '" + database.getName().trim() + "'");
        ArrayList<Table> tables = new ArrayList<>();
        try {
            while (res.next()) {
                tables.add(new Table(res.getString("table_name"), database));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.access.closeConnection();
        }
        return tables;
    }

    @Override
    public ArrayList<Row> getRows(Table table) throws DaoAccessException {
        this.access.execute("use " + table.getDatabase().getName());
        ResultSet res = this.access.execute("select * from " + table.getName());
        return this.getRows(res);
    }

    @Override
    public ArrayList<Column> getAttributes(Table table) throws DaoAccessException {
        ResultSet result = this.access.execute(
                "select column_name, data_type from information_schema.columns where table_schema = '" +
                        table.getDatabase().getName() + "' and table_name = '" + table.getName() + "'");
        return this.getAttributes(result);
    }

    @Override
    public ArrayList<ForeignKey> getForeignKeys(Table table) throws DaoAccessException {
        ResultSet rs = this.access.execute("SELECT COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA = '" + table.getDatabase().getName() + "' " +
                "AND TABLE_NAME = '" + table.getName() + "' " +
                "AND REFERENCED_TABLE_NAME IS NOT NULL");
        ArrayList<ForeignKey> keys = new ArrayList<>();
        try {
            while (rs.next()) {
                keys.add(new ForeignKey(rs.getString("COLUMN_NAME"),
                        rs.getString("REFERENCED_TABLE_NAME"),
                        rs.getString("REFERENCED_COLUMN_NAME")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.access.closeConnection();
        }
        return keys;
    }

    @Override
    public ArrayList<String> getPrimaryKey(Table table) throws DaoAccessException {
        ResultSet res = this.access.execute("SELECT k.COLUMN_NAME " +
                "FROM information_schema.table_constraints t " +
                "LEFT JOIN information_schema.key_column_usage k " +
                "USING(constraint_name,table_schema,table_name) " +
                "WHERE t.constraint_type='PRIMARY KEY' " +
                "AND t.table_schema='" + table.getDatabase().getName() + "' " +
                "AND t.table_name='" + table.getName() + "'");
        ArrayList<String> keys = new ArrayList<>();
        try {
            while (res.next()) {
                keys.add(res.getString("k.COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoAccessException(e);
        } finally {
            this.access.closeConnection();
        }
        return keys;
    }

    @Override
    public Row getParentRow(ForeignKey foreignKey, Row row, Table table) throws DaoAccessException {
        ResultSet res = this.access.execute(
                "select * from " + foreignKey.getReferencedTableName() +
                        " where " + foreignKey.getReferencedColumnName() + " = '" +
                        row.getAttribute(foreignKey.getName()) + "'");
        return this.getParentRow(res);
    }

    @Override
    public void modifyRow(Row row, Table table) throws DaoAccessException {
        List<Column> attributes = table.getAttributes();
        List<ForeignKey> fk = table.getForeignKeys();
        List<Column> pk = table.getPrimaryKeys();
        StringBuilder builder = new StringBuilder("update " + table.getName() + " set ");
        this.appendList(row, builder, attributes);
        if (attributes.size() > 0 && fk.size() > 0) builder.append(", ");
        this.appendList(row, builder, fk);
        builder.append("where ");
        for (int i = 0; i < pk.size() - 1; i++) {
            this.appendAttribute(row, builder, pk.get(i));
            builder.append(" and ");
        }
        if (pk.size() > 0 )this.appendAttribute(row, builder, pk.get(pk.size() - 1));
        this.access.executeUpdate(new String(builder));
    }

    private void appendList(Row row, StringBuilder builder, List<? extends Column> columns) {
        for (int i = 0; i < columns.size() - 1; i++) {
            this.appendAttribute(row, builder, columns.get(i));
            builder.append(", ");
        }
        if (columns.size() > 0)this.appendAttribute(row, builder, columns.get(columns.size() - 1));
        builder.append(" ");
    }

    private void appendAttribute(Row row, StringBuilder builder, Column column) {
        String s = Type.isString(column.getSqlType()) ? "'" : "";
        builder.append(column.getName()).append(" = ").append(s).append(row.getAttribute(column.getName())).append(s);
    }

    @Override
    public void dropDatabase(Database database) throws DaoAccessException {
        this.access.executeUpdate("drop database " + database.getName());
    }

    @Override
    public void truncateTable(Table table) throws DaoAccessException {
        this.access.executeUpdate("truncate table " + table.getName());
    }

    @Override
    public void addTable(Table table) throws DaoAccessException {
        //start of the query
        StringBuilder query = new StringBuilder("create table ").append(table.getName()).append(" (");
        //primary key(s)
        List<Column> primaryKeys = table.getPrimaryKeys();
        if (primaryKeys.isEmpty()) throw new DaoAccessException("no primary key");
        else if (primaryKeys.size() == 1) {
            Column pk = primaryKeys.get(0);
            query.append(pk.getName()).append(" ").append(pk.getType()).append(", primary key (")
                    .append(pk.getName()).append(")");
        } else {
            for (Column pk : primaryKeys) {
                query.append(pk.getName()).append(" ").append(pk.getType()).append(", ");
            }
            query.append("primary key (");
            for (int i = 0; i < primaryKeys.size() - 1; i++) {
                query.append(primaryKeys.get(i)).append(", ");
            }
            query.append(primaryKeys.get(primaryKeys.size() - 1)).append(")");
        }
        //foreign key(s)
        List<ForeignKey> foreignKeys = table.getForeignKeys();
        for (ForeignKey fk : foreignKeys) {
            query.append(", ").append(fk.getName()).append(" ").append(fk.getType()).append(" references ")
                    .append(fk.getReferencedTableName()).append("(").append(fk.getReferencedColumnName()).append(")");
        }
        //other column(s)
        List<Column> columns = table.getAttributes();
        for (Column column : columns) {
            query.append(", ").append(column.getName()).append(" ").append(column.getType());
            if (column.isUnique()) query.append(" UNIQUE");
            if (!column.isNullable()) query.append(" NOT NULL");
        }
        //end of the query
        query.append(")");
        System.out.println(query);
        this.access.executeUpdate(new String(query));
        this.access.closeConnection();
    }

    @Override
    public void dropTable(Table table) throws DaoAccessException {
        this.access.executeUpdate("drop table " + table.getName());
        this.access.closeConnection();
    }

    @Override
    public void addRow(Table table, Row row) throws DaoAccessException {
        ArrayList<Column> tableAttr = this.getAttributes(table);
        StringBuilder query = new StringBuilder("insert into " + table.getName() + " values (");
        if (Type.isString(tableAttr.get(0).getSqlType())) query.append("'");
        for (int i = 0; i < tableAttr.size() - 1; i++) {
            boolean stringAttr = Type.isString(tableAttr.get(i).getSqlType());
            boolean stringNextAttr = Type.isString(tableAttr.get(i + 1).getSqlType());
            query.append(row.getAttribute(tableAttr.get(i).getName()));
            if (stringAttr) query.append("'");
            query.append(", ");
            if (stringNextAttr) query.append("'");
        }
        query.append(row.getAttribute(tableAttr.get(tableAttr.size() - 1).getName()));
        if (Type.isString(tableAttr.get(tableAttr.size() - 1).getSqlType()))
            query.append("'");
        query.append(")");
        this.access.executeUpdate(new String(query));
        this.access.closeConnection();
    }

    @Override
    public Table execute(String query) throws DaoAccessException, SQLException {
        ResultSet rs = this.access.execute(query);
        Table table = new Table("table");
        ResultSetMetaData metaData = rs.getMetaData();
        ArrayList<Column> columns = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            Column c = new Column();
            c.setName(metaData.getColumnName(i));
            columns.add(c);
        }
        table.setAttributes(columns);
        ArrayList<Row> rows = new ArrayList<>();
        while (rs.next()) {
            Row row = new Row();
            HashMap<String, Object> values = new HashMap<>();
            for (Column c : table.getAttributes()) {
                values.put(c.getName(), rs.getString(c.getName()));
            }
            row.setAttributes(values);
            rows.add(row);
        }
        table.setRows(rows);
        return table;
    }

    @Override
    public void simpleExecution(String query) throws DaoAccessException {
        this.access.execute(query);
    }

    @Override
    public void delete(Table table, Row row) throws DaoAccessException {
        StringBuilder builder = new StringBuilder("delete from " + table.getName() + " where ");
        List<Column> pk = table.getPrimaryKeys();
        if (pk.isEmpty()) throw new DaoAccessException("Primary keys list cannot be empty");
        for (Column c : pk) {
            builder.append(c.getName()).append(" = ");
            if (Type.isString(c.getSqlType())) builder.append("'").append(row.getAttribute(c.getName())).append("'");
            else builder.append(row.getAttribute(c.getName()));
            builder.append(" and ");
        }
        String query = new String(builder);
        this.access.executeUpdate(query.substring(0, query.length() - 5));
    }

    @Autowired
    public void setAccess(DatabaseAccess access) {
        this.access = access;
    }

    @Override
    public void addAdminSchema(String schemaName, String password) throws DaoAccessException {
        if (schemaName == null || schemaName.equalsIgnoreCase(""))
            throw new DaoAccessException("Please enter a name for the new database");
        this.access.executeUpdate("create database " + schemaName);
    }

    @Override
    public void addStandardSchema(String schemaName, String password) throws DaoAccessException {
        this.addAdminSchema(schemaName, password);
    }

    @Override
    public void addMinimalSchema(String schemaName, String password) throws DaoAccessException {
        this.addAdminSchema(schemaName, password);

    }

    @Override
    public void addColumn(Table table, Column column) throws DaoAccessException {
        StringBuilder builder = new StringBuilder("alter table " + table.getDatabase() + "." + table.getName() +
                " add " + column.getName() + " " + column.getType());
        if (!column.isNullable()) builder.append(" not null");
        if (column.isUnique()) builder.append(" unique");
        if (column instanceof ForeignKey) {
            builder.append(" references ").append(table.getDatabase().getName()).append(".")
                    .append(((ForeignKey) column).getReferencedTableName()).append("(")
                    .append(((ForeignKey) column).getReferencedColumnName()).append(")");
        }
        this.access.executeUpdate(new String(builder));
    }

    @Override
    public void removeColumn(Table table, Column column) throws DaoAccessException {
        this.access.executeUpdate("alter table " + table.getName() + " drop column " + column.getName());
    }
}
