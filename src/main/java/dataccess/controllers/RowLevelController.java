package dataccess.controllers;

import dataccess.model.Row;
import dataccess.model.Table;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class RowLevelController {

    @Getter
    private Row row;
    @Getter
    private Table table;
    private MainController mainController;

    public void setRow(Row row) {
        this.row = row;
        this.mainController.setCurrentRow(row);
    }

    public void setTable(Table table) {
        this.table = table;
        this.mainController.setCurrentTable(table);
    }

    @Autowired
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
