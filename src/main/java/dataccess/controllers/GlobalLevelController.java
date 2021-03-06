package dataccess.controllers;

import dataccess.dao.DaoAccessException;
import dataccess.model.Database;
import dataccess.service.QueryService;
import lombok.Setter;
import org.primefaces.model.DefaultTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;

@Controller
public class GlobalLevelController {

    private QueryService service;
    @Setter
    private ArrayList<Database> databases = new ArrayList<>();
    private MainController controller;

    public void dropDatabase(Database database) {
        try {
            this.service.dropDatabase(database);
            FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "schema successfully dropped", ""));
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
    }

    public ArrayList<Database> getDatabases() {
        if (databases.isEmpty()) this.setUp();
        return databases;
    }

    public void setUp() {
        try {
            this.databases = this.service.getDatabases();
            DefaultTreeNode root = this.controller.getTree();
            root.getChildren().clear();
            for (Database d : this.databases) {
                root.getChildren().add(new DefaultTreeNode(d));
            }
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
    }

    @Autowired
    public void setService(QueryService service) {
        this.service = service;
    }

    @PostConstruct
    private void afterPropertiesSet() {
        this.setUp();
    }

    @Autowired
    public void setController(MainController controller) {
        this.controller = controller;
    }
}
