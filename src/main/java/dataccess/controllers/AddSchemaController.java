package dataccess.controllers;

import dataccess.dao.DaoAccessException;
import dataccess.model.Database;
import dataccess.service.QueryService;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.Arrays;
import java.util.List;


@Controller
@Getter @Setter
public class AddSchemaController {

    private QueryService service;
    private MainController mainController;
    private String schemaName;
    private String password;
    private Right right;

    public enum Right{
        ADMIN, STANDARD, CONSULTATION
    }

    public List<Right> getRights() {
        return Arrays.asList(Right.values().clone());
    }

    public String newSchema() {
        this.schemaName = "";
        this.password = null;
        this.right = Right.STANDARD;
        return "new-schema";
    }

    public void addSchema() {
        try {
            switch (this.right) {
                case ADMIN:
                    this.service.addAdminSchema(this.schemaName, this.password);
                    break;
                case STANDARD:
                    this.service.addStandardSchema(this.schemaName, this.password);
                    break;
                case CONSULTATION:
                    this.service.addMinimalSchema(this.schemaName, this.password);
            }
            FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "schema successfully added", ""));
            this.mainController.getTree().getChildren().add(new DefaultTreeNode(new Database(this.schemaName)));
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
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
}
