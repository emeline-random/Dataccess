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
import java.util.Collection;

@Controller
public class DatabaseLevelController {

    @Getter
    private Database database;
    private QueryService service;
    private MainController mainController;

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
        if (this.database == null) {
            this.database = database;
            this.mainController.setCurrentBase(database);
            this.setUp();
        }
        return "see-database";
    }

    @Autowired
    public void setService(QueryService service) {
        this.service = service;
    }

    @Autowired
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
