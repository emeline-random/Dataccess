package dataccess.service;

import dataccess.dao.DaoAccessException;
import dataccess.model.*;

import java.sql.SQLException;
import java.util.ArrayList;

public interface QueryService {

    void completeTable(Table table) throws DaoAccessException;

    ArrayList<Database> getDatabases() throws DaoAccessException;

    ArrayList<Table> getTables(Database database) throws DaoAccessException;

    ArrayList<Row> getRows(Table table) throws DaoAccessException;

    ArrayList<Column> getAttributes(Table table) throws DaoAccessException;

    ArrayList<ForeignKey> getForeignKeys(Table table) throws DaoAccessException;

    ArrayList<String> getPrimaryKey(Table table) throws DaoAccessException;

    Row getParentRow(ForeignKey foreignKey, Row row) throws DaoAccessException;

    void addTable(Table table) throws DaoAccessException;

    void dropTable(Table table) throws DaoAccessException;

    void addRow (Table table, Row row) throws DaoAccessException;

    Table execute (String query) throws DaoAccessException, SQLException;

    void simpleExecution(String query) throws DaoAccessException;

    void delete(Table table, Row row) throws DaoAccessException;

}
