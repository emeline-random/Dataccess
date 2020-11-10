package dataccess.controllers;

import dataccess.model.Database;
import dataccess.service.QueryService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;


@Controller
@Getter @Setter
public class AddSchemaController {

    private QueryService service;
    private Database newDatabase;
    private boolean administrator;
    private boolean standard;

    /**
     * GRANT CREATE session, CONNECT, CREATE table, CREATE view,
     *       CREATE procedure,CREATE synonym,
     *       ALTER table, ALTER view, ALTER procedure,ALTER synonym,
     *       DROP table, DROP view, DROP procedure,DROP synonym,
     *       SELECT any table, INSERT any table, DELETE any table, DROP any table
     *       TO user;
     *       alter user data_owner quota unlimited on default_tablespace;
     * @return
     */


    public String newSchema() {
        this.newDatabase = new Database("null");
        return "new-schema";
    }

    @Autowired
    public void setService(QueryService service) {
        this.service = service;
    }
}
