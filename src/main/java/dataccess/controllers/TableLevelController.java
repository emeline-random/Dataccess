package dataccess.controllers;

import dataccess.dao.DaoAccessException;
import dataccess.model.Column;
import dataccess.model.ForeignKey;
import dataccess.model.Row;
import dataccess.model.Table;
import dataccess.service.QueryService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Controller
@Getter
@Setter
public class TableLevelController {

    private Table table;
    private Row row;
    private QueryService service;
    private RowLevelController rowLevelController;
    private MainController mainController;
    private DatabaseLevelController databaseLevelController;
    private Column column;
    private Column columnToRemove;
    private Integer insertValue = 1;
    private final List<Integer> insertValues = new ArrayList<>(Arrays.asList(1, 3, 5, 10, 15));
    private final ArrayList<Row> insertedRows = new ArrayList<>();
    private boolean modifyRow, addColumn;

    public void setInsertValue(Integer insertValue) {
        this.insertValue = insertValue;
        int size = this.insertedRows.size();
        if (size != insertValue) {
            if (size < insertValue) {
                for (int i = size; i < insertValue; i++) {
                    this.insertedRows.add(this.initRow());
                }
            } else {
                this.insertedRows.subList(insertValue, size).clear();
            }
        }
    }

    public ArrayList<Row> getRows() {
        try {
            if (this.table.getRows() == null || this.table.getRows().isEmpty()) {
                this.table.setRows(this.service.getRows(this.table));
            }
            return this.table.getRows();
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
        return new ArrayList<>();
    }

    public String seeParentRow(String attr, Row row) {
        try {
            Row parent = this.service.getParentRow(this.table.getForeignKey(attr), row, this.table);
            this.rowLevelController.setRow(parent);
            Table table = new Table(this.table.getForeignKey(attr).getReferencedTableName(), this.table.getDatabase());
            this.rowLevelController.setTable(table);
            this.mainController.setCurrentTable(table);
            this.table = table;
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
        return "see-row";
    }

    public String seeRow(Row row) {
        this.row = row;
        this.rowLevelController.setRow(row);
        this.rowLevelController.setTable(this.getTable());
        return "see-row";
    }

    public String setTable(Table table) {
        this.row = null;
        this.table = table;
        try {
            this.service.completeTable(this.table);
            this.insertedRows.clear();
            this.setInsertValue(this.insertValue);
        } catch (DaoAccessException throwables) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    throwables.getMessage(), ""));
            return "see-table";
        }
        this.mainController.setCurrentTable(this.table);
        return "see-table";
    }

    public String structure() {
        return "structure";
    }

    public void modifyRow() {
        try {
            this.service.modifyRow(this.row, this.table);
            this.modifyRow = false;
            FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Row successfully modified", ""));
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
    }

    public void createColumn() {
        this.column = new Column();
        this.addColumn = true;
    }

    public String addColumn() {
        try {
            this.service.addColumn(this.table, this.column);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Column successfully added", ""));
            if (this.column instanceof ForeignKey) {
                this.table.getForeignKeys().add((ForeignKey) this.column);
            } else {
                this.table.getAttributes().add(this.column);
            }
            this.column = null;
            this.addColumn = false;
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
        return this.structure();
    }

    public void cancelAddColumn() {
        this.column = new Column();
        this.addColumn = false;
    }

    public void removeColumn() {
        if (this.columnToRemove != null) {
            try {
                this.service.removeColumn(this.table, this.columnToRemove);
                FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "column deleted successfully", ""));
                this.table.removeColumn(this.columnToRemove);
                this.columnToRemove = new Column();
            } catch (DaoAccessException e) {
                FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(), ""));
            }
        }
    }

    public void setColumnName(String columnName) {
        this.columnToRemove = this.table.getColumn(columnName);
    }

    public String getColumnName() {
        return this.columnToRemove == null ? null : this.columnToRemove.getName();
    }

    public void switchToForeignKey() {
        this.column = new ForeignKey(this.column);
    }

    public void switchToClassicalColumn() {
        if (this.column instanceof ForeignKey) {
            this.column = new Column((ForeignKey) this.column);
        }
    }

    public void dropRow(Row row) {
        try {
            this.service.delete(this.table, row);
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "row deleted successfully", ""));
            ArrayList<Row> rows = this.table.getRows();
            rows.remove(row);
            this.table.setRows(rows);
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
    }

    public void dropTable(Table table) {
        try {
            this.service.dropTable(table);
            FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "table dropped successfully", ""));
            this.databaseLevelController.setUp();
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
    }

    public String insert() {
        this.createRow();
        return "insert";
    }

    public void createRow() {
        this.row = this.initRow();
    }

    private Row initRow() {
        Row row = new Row();
        HashMap<String, Object> map = new HashMap<>();
        for (Column key : this.table.getPrimaryKeys()) map.put(key.getName(), null);
        for (Column key : this.table.getAttributes()) map.put(key.getName(), null);
        for (ForeignKey key : this.table.getForeignKeys()) map.put(key.getName(), null);
        row.setAttributes(map);
        return row;
    }

    public void addRows() {
        for (Row row : this.insertedRows) {
            this.addRow(row);
        }
        this.insertedRows.replaceAll(row1 -> row1 = this.initRow());
    }

    public void addRow() {
        this.addRow(this.row);
        this.row = null;
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        if (viewId.contains("insert")) this.createRow();
    }

    public void clear() {
        try {
            this.service.truncateTable(this.table);
            FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "table truncated successfully", ""));
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
    }

    public String dropTable() {
        this.dropTable(this.table);
        return "see-database";
    }

    public void addRow(Row row) {
        try {
            row.convert();
            this.service.addRow(this.table, row);
            ArrayList<Row> rows = this.table.getRows();
            rows.add(row);
            FacesContext.getCurrentInstance().addMessage("success", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "row added successfully", ""));
            HashMap<String, Object> map = row.getAttributes();
            map.forEach((s, o) -> o = null);
            row.setAttributes(map);
        } catch (DaoAccessException e) {
            FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
    }

    public String operations() {
        return "operations";
    }

    public ArrayList<Column> getColumns() {
        ArrayList<Column> rows = new ArrayList<>(this.table.getPrimaryKeys());
        rows.addAll(this.table.getForeignKeys());
        rows.addAll(this.table.getAttributes());
        return rows;
    }

    @Autowired
    public void setService(QueryService service) {
        this.service = service;
    }

    @Autowired
    public void setRowLevelController(RowLevelController rowLevelController) {
        this.rowLevelController = rowLevelController;
    }

    @Autowired
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Autowired
    public void setDatabaseLevelController(DatabaseLevelController databaseLevelController) {
        this.databaseLevelController = databaseLevelController;
    }
}
