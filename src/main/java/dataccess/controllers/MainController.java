package dataccess.controllers;

import dataccess.components.Tree;
import dataccess.dao.DaoAccessException;
import dataccess.model.Column;
import dataccess.model.Database;
import dataccess.model.Row;
import dataccess.model.Table;
import dataccess.service.QueryService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;

@Controller
public class MainController {

    private DatabaseLevelController databaseLevelController;
    private TableLevelController tableLevelController;
    private RowLevelController rowLevelController;
    private GlobalLevelController globalLevelController;
    @Getter
    @Setter
    private Database currentBase;
    @Getter
    @Setter
    private Table currentTable;
    @Getter
    @Setter
    private Row currentRow;
    private QueryService service;
    @Getter @Setter
    private Tree tree = new Tree("Root", null);

    public Object getCurrentRowPK() {
        try {
            if (this.currentTable.getPrimaryKeys() == null) {
                this.service.completeTable(this.currentTable);
            }
            List<Column> keys = this.currentTable.getPrimaryKeys();
            if (keys.size() == 1)
                return this.currentRow.getAttribute(this.currentTable.getPrimaryKeys().get(0).getName());
            else {
                StringBuilder builder = new StringBuilder("(");
                for (int i = 0; i < keys.size() - 1; i++) {
                    builder.append(this.currentRow.getAttribute(keys.get(i).getName())).append(", ");
                }
                builder.append(this.currentRow.getAttribute(keys.get(keys.size() - 1).getName())).append(")");
                return builder;
            }
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
        return null;
    }

    public String home() {
        this.currentBase = null;
        this.currentTable = null;
        this.currentRow = null;
        this.globalLevelController.setUp();
        return "home";
    }

    public String seeBase() {
        this.databaseLevelController.setDatabase(this.currentBase);
        this.currentTable = null;
        this.currentRow = null;
        return "see-database";
    }

    public String seeTable() {
        this.tableLevelController.setTable(this.currentTable);
        this.currentRow = null;
        return "see-table";
    }

    public String seeRow() {
        this.rowLevelController.setTable(this.currentTable);
        this.rowLevelController.setRow(this.currentRow);
        return "see-row";
    }

    public String query() {
        return "query-result";
    }

    public String nodeRedirection(Object node ) throws IOException { //TODO forcer le rechargement de la table car ne change rien si on ne cahnge pas de page
        if (node instanceof Database) {
            this.databaseLevelController.setDatabase((Database) node);
            return "see-database";
        } else if (node instanceof Table){
            this.tableLevelController.setTable((Table) node);
            return "see-table";
        }
        return null;
    }

    @Autowired
    public void setRowLevelController(RowLevelController rowLevelController) {
        this.rowLevelController = rowLevelController;
    }

    @Autowired
    public void setTableLevelController(TableLevelController tableLevelController) {
        this.tableLevelController = tableLevelController;
    }

    @Autowired
    public void setDatabaseLevelController(DatabaseLevelController databaseLevelController) {
        this.databaseLevelController = databaseLevelController;
    }

    @Autowired
    public void setGlobalLevelController(GlobalLevelController globalLevelController) {
        this.globalLevelController = globalLevelController;
    }

    @Autowired
    public void setService(QueryService service) {
        this.service = service;
    }
}
