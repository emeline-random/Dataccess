package dataccess.controllers;

import dataccess.dao.DaoAccessException;
import dataccess.model.Database;
import dataccess.model.Table;
import dataccess.service.QueryService;
import lombok.Getter;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Collection;

@Controller
public class DatabaseLevelController {

    @Getter
    private Database database;
    private QueryService service;
    private MainController mainController;
    private GlobalLevelController globalLevelController;

    public Collection<Table> getTables() {
        return this.database.getTables();
    }

    void setUp() {
        try {
            this.database.setTables(this.service.getTables(this.database));
            if (this.mainController.getTree().getNode(this.database.getName()).getChildren().isEmpty()) {
                TreeNode node = this.mainController.getTree().getNode(this.database.getName());
                for (Table t : this.database.getTables()) {
                    node.getChildren().add(new DefaultTreeNode(t));
                }
            }
        } catch (DaoAccessException throwables) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    throwables.getMessage(), ""));
        }
    }

    public String setDatabase(Database database) {
        this.database = database;
        this.mainController.setCurrentBase(database);
        this.setUp();
        return this.seeTables();
    }

    public String seeTables() {
        return "see-database";
    }

    public String operations() {
        return "database-operations";
    }

    public String dropDatabase() {
        try {
            this.service.dropDatabase(this.database);
            FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "database dropped successfully", ""));
            this.globalLevelController.getDatabases().removeIf(d -> this.database.getName().equalsIgnoreCase(d.getName()));
            TreeNode n = this.mainController.getTree().getNode(this.database.getName());
            this.mainController.getTree().getChildren().remove(n);
            return "home";
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
            return "see-database";
        }
    }

    public void dropAllTables() {
        for (Table t : this.database.getTables()) {
            try {
                this.service.dropTable(t);
                FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "table " + t.getName() + " dropped successfully", ""));
                this.database.setTables(new ArrayList<>());
            } catch (DaoAccessException e) {
                FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(), ""));
            }
        }
    }

    @Autowired
    public void setService(QueryService service) {
        this.service = service;
    }

    @Autowired
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Autowired
    public void setGlobalLevelController(GlobalLevelController globalLevelController) {
        this.globalLevelController = globalLevelController;
    }
}
