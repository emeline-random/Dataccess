package dataccess.controllers;


import dataccess.dao.DaoAccessException;
import dataccess.model.Column;
import dataccess.model.Database;
import dataccess.model.ForeignKey;
import dataccess.model.Table;
import dataccess.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;

@Controller
public class AddTableController {

    private Table table;
    private QueryService service;
    private TableLevelController tableLevelController;
    private DatabaseLevelController databaseLevelController;

    public String createTable() throws DaoAccessException {
        this.service.addTable(this.table);
        this.databaseLevelController.setUp();
        return this.tableLevelController.setTable(this.table);
    }

    public void addColumn() {
        this.table.addColumn(new Column());
    }

    public void addPrimaryKey() {
        this.table.addPrimaryKey(new Column());
    }

    public void addForeignKey() {
        this.table.addForeignKey(new ForeignKey());
    }

    public void previewTable() {
        this.table.print();
    }

    public Table getTable() {
        return table;
    }

    public String addTable() {
        this.table = new Table("table name", this.databaseLevelController.getDatabase());
        this.table.setPrimaryKeys(new ArrayList<>());
        this.table.setForeignKeys(new ArrayList<>());
        this.table.setAttributes(new ArrayList<>());
        return "add-table";
    }

    public void removeColumn(Column column) {
        this.table.removeColumn(column);
    }

    public void removeFK(ForeignKey foreignKey) {
        this.table.removeForeignKey(foreignKey);
    }

    public void removePK(Column primaryKey) {
        this.table.removePrimaryKey(primaryKey);
    }

    @Autowired
    public void setTableLevelController(TableLevelController tableLevelController) {
        this.tableLevelController = tableLevelController;
    }

    @Autowired
    public void setService(QueryService service) {
        this.service = service;
    }

    @Autowired
    public void setDatabaseLevelController(DatabaseLevelController databaseLevelController) {
        this.databaseLevelController = databaseLevelController;
    }
}
