package dataccess.controllers;


import dataccess.dao.DaoAccessException;
import dataccess.model.Column;
import dataccess.model.ForeignKey;
import dataccess.model.Table;
import dataccess.service.QueryService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;

@Controller
public class AddTableController {

    private Table table;
    private QueryService service;
    private TableLevelController tableLevelController;
    private DatabaseLevelController databaseLevelController;
    @Getter
    private String preview = "";

    public String createTable() {
        try {
            this.service.addTable(this.table);
            FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "table " + this.table.getDatabase().getName() + "." + this.table.getName() +
                            " successfully created", ""));
            return this.tableLevelController.setTable(this.table);
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
        this.databaseLevelController.setUp();
        return null;
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
        this.preview = this.table.print();
    }

    public Table getTable() {
        return table;
    }

    public String addTable() {
        this.table = new Table("table name", this.databaseLevelController.getDatabase());
        this.table.setPrimaryKeys(new ArrayList<>());
        this.table.setForeignKeys(new ArrayList<>());
        this.table.setAttributes(new ArrayList<>());
        preview = "";
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
