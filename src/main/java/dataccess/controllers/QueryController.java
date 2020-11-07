package dataccess.controllers;

import dataccess.dao.DaoAccessException;
import dataccess.model.Table;
import dataccess.service.QueryService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.sql.SQLException;


@Controller
@Scope(value = "session")
public class QueryController {

    @Getter
    @Setter
    private Table currentResult = new Table("table");
    private QueryService service;
    @Getter
    @Setter
    private String currentQuery;

    public String execute() {
        if (this.currentQuery.toLowerCase().contains("select")) {
            this.currentResult = this.executeSelectQuery();
            return "query-result";
        } else {
            this.executeWithoutSelect();
            return "";
        }
    }

    public String executeTableQuery() {
        this.execute();
        return "table-query";
    }

    private Table executeSelectQuery() {
        try {
            return this.service.execute(this.currentQuery);
        } catch (DaoAccessException | SQLException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
        return new Table("table");
    }

    private void executeWithoutSelect() { //FIXME execute deux fois la commande
        try {
            this.service.simpleExecution(this.currentQuery);
            FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "query executed successfully", ""));
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
    }

    public String showTableQuery() {
        return "table-query";
    }

    @Autowired
    public void setService(QueryService service) {
        this.service = service;
    }
}
