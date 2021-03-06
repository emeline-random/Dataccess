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
public class OracleService implements dataccess.service.QueryService {
    //pour éviter les problèmes de table non trouvée en fonction de l'utilisateur connecté

    private DatabaseAccess access;

    @Override
    public ArrayList<Database> getDatabases() throws DaoAccessException {
        ResultSet res = this.access.execute("select username as schema_name\n" +
                "from sys.all_users\n" +
                "order by username");
        ArrayList<Database> databases = new ArrayList<>();
        try {
            while (res.next()) {
                if (res.getString("schema_name").contains("C##"))
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
        ResultSet res = this.access.execute(
                "select distinct OBJECT_NAME from ALL_OBJECTS where OBJECT_TYPE = 'TABLE' and OWNER = '" +
                        database.getName().trim() + "'");
        ArrayList<Table> tables = new ArrayList<>();
        try {
            while (res.next()) {
                tables.add(new Table(res.getString("OBJECT_NAME"), database));
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
        ResultSet res = this.access.execute("select * from " + table.getDatabase().getName() + "."
                + table.getName());
        return this.getRows(res);
    }

    @Override
    public ArrayList<Column> getAttributes(Table table) throws DaoAccessException {
        ResultSet result = this.access.execute("SELECT column_name, data_type\n" +
                "FROM ALL_TAB_COLUMNS\n" +
                "WHERE table_name = '" + table.getName() + "' " +
                "AND upper(owner)='" + table.getDatabase().getName().toUpperCase() + "'");
        return this.getAttributes(result);
    }

    @Override
    public ArrayList<ForeignKey> getForeignKeys(Table table) throws DaoAccessException {
        ResultSet rs = this.access.execute("SELECT DISTINCT a.column_name child_column, b.table_name parent_table, b.column_name parent_column\n" +
                " FROM all_cons_columns a" +
                " JOIN all_constraints c ON a.owner = c.owner AND a.constraint_name = c.constraint_name" +
                " join all_cons_columns b on c.owner = b.owner and c.r_constraint_name = b.constraint_name" +
                " WHERE c.constraint_type = 'R'\n" +
                " AND a.table_name = '" + table.getName() + "'" +
                " AND upper(a.owner)='" + table.getDatabase().getName() + "'");
        ArrayList<ForeignKey> keys = new ArrayList<>();
        try {
            while (rs.next()) {
                keys.add(new ForeignKey(rs.getString("child_column"), rs.getString("parent_table"),
                        rs.getString("parent_column")));
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
        ResultSet res = this.access.execute("SELECT column_name FROM all_cons_columns WHERE constraint_name = (" +
                " SELECT constraint_name FROM all_constraints \n" +
                " WHERE UPPER(table_name) = UPPER('" + table.getName().toUpperCase() + "') AND CONSTRAINT_TYPE = 'P'" +
                " AND UPPER(owner)='" + table.getDatabase().getName().toUpperCase() + "'" +
                ")");
        ArrayList<String> keys = new ArrayList<>();
        try {
            while (res.next()) {
                keys.add(res.getString("column_name"));
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
    public void truncateTable(Table table) throws DaoAccessException {
        this.access.executeUpdate("truncate table " + table.getDatabase().getName() + "." + table.getName());
    }

    @Override
    public Row getParentRow(ForeignKey foreignKey, Row row, Table table) throws DaoAccessException {
        ResultSet res = this.access.execute(
                "select * from " + table.getDatabase().getName() + "." + foreignKey.getReferencedTableName() +
                        " where " + foreignKey.getReferencedColumnName() + " = '" +
                        row.getAttribute(foreignKey.getName()) + "'");
        return this.getParentRow(res);
    }

    @Override
    public void modifyRow(Row row, Table table) throws DaoAccessException { //FIXME table inexistante
        List<Column> attributes = table.getAttributes();
        List<ForeignKey> fk = table.getForeignKeys();
        List<Column> pk = table.getPrimaryKeys();
        StringBuilder builder = new StringBuilder("update " + table.getDatabase() + "." + table.getName() + " set ");
        this.appendList(row, builder, attributes);
        if (attributes.size() > 0 && fk.size() > 0) builder.append(", ");
        this.appendList(row, builder, fk);
        builder.append("where ");
        for (int i = 0; i < pk.size() - 1; i++) {
            this.appendAttribute(row, builder, pk.get(i));
            builder.append(" and ");
        }
        if (pk.size() > 0) this.appendAttribute(row, builder, pk.get(pk.size() - 1));
        this.access.executeUpdate(new String(builder));
    }

    private void appendList(Row row, StringBuilder builder, List<? extends Column> columns) {
        for (int i = 0; i < columns.size() - 1; i++) {
            this.appendAttribute(row, builder, columns.get(i));
            builder.append(", ");
        }
        if (columns.size() > 0) this.appendAttribute(row, builder, columns.get(columns.size() - 1));
        builder.append(" ");
    }

    private void appendAttribute(Row row, StringBuilder builder, Column column) {
        String s = Type.isString(column.getSqlType()) ? "'" : "";
        builder.append(column.getName()).append(" = ").append(s).append(row.getAttribute(column.getName())).append(s);
    }

    @Override
    public void dropDatabase(Database database) throws DaoAccessException {
        this.access.execute("drop user " + database.getName() + " cascade");
    }

    @Override
    public void addTable(Table table) throws DaoAccessException {
        //start of the query
        StringBuilder query = new StringBuilder("create table ").append(table.getDatabase().getName())
                .append(".").append(table.getName()).append(" (");
        //primary key(s)
        List<Column> primaryKeys = table.getPrimaryKeys();
        if (primaryKeys.isEmpty()) throw new DaoAccessException("no primary key");
        else if (primaryKeys.size() == 1) {
            Column pk = primaryKeys.get(0);
            query.append(pk.getName()).append(" ").append(pk.getType()).append(" constraint pk").append(table.getName())
                    .append(" primary key");
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
        this.access.execute(new String(query));
        this.access.closeConnection();
    }

    @Override
    public void dropTable(Table table) throws DaoAccessException {
        this.access.execute("drop table " + table.getDatabase().getName() + "." + table.getName());
        this.access.closeConnection();
    }

    @Override
    public void addRow(Table table, Row row) throws DaoAccessException {
        ArrayList<Column> tableAttr = this.getAttributes(table);
        StringBuilder query = new StringBuilder("insert into " + table.getDatabase().getName() + "." +
                table.getName() + " values (");
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
        this.access.execute(new String(query));
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
        StringBuilder builder = new StringBuilder("delete from " + table.getDatabase() + "." + table.getName() + " where ");
        List<Column> pk = table.getPrimaryKeys();
        if (pk.isEmpty()) throw new DaoAccessException("Primary keys list cannot be empty");
        for (Column c : pk) {
            builder.append(c.getName()).append(" = ");
            if (Type.isString(c.getSqlType())) builder.append("'").append(row.getAttribute(c.getName())).append("'");
            else builder.append(row.getAttribute(c.getName()));
            builder.append(" and ");
        }
        String query = new String(builder);
        this.access.execute(query.substring(0, query.length() - 5));
    }

    @Autowired
    public void setAccess(DatabaseAccess access) {
        this.access = access;
    }

    @Override
    public void addAdminSchema(String schemaName, String password) throws DaoAccessException {
        this.createUser(schemaName, password);
        this.access.execute("GRANT CREATE session, CONNECT, CREATE table, CREATE view," +
                " CREATE procedure,CREATE synonym," +
                " SELECT any table, INSERT any table, DELETE any table, DROP any table," +
                " UNLIMITED TABLESPACE" +
                " TO " + schemaName);
    }

    @Override
    public void addStandardSchema(String schemaName, String password) throws DaoAccessException {
        this.createUser(schemaName, password);
        this.access.execute("GRANT CREATE session, CONNECT, CREATE table, CREATE view," +
                " UNLIMITED TABLESPACE, SELECT any table" +
                " TO " + schemaName);
    }

    @Override
    public void addMinimalSchema(String schemaName, String password) throws DaoAccessException {
        this.createUser(schemaName, password);
        this.access.execute("GRANT CREATE session, CONNECT, SELECT any table to " + schemaName);
    }

    private void createUser(String schemaName, String password) throws DaoAccessException {
        if (schemaName == null || !schemaName.toUpperCase().startsWith("C##")) throw new DaoAccessException(
                "Schema name must begin with \"C##\" !");
        if (password != null && !password.trim().equalsIgnoreCase(""))
            this.access.execute("create user " + schemaName + " identified by " + password);
        else throw new DaoAccessException("Users must have a password to connect to the database");
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
        this.access.executeUpdate("alter table " + table.getDatabase() +"."+ table.getName() + " drop" +
                " column " + column.getName());
    }
}
