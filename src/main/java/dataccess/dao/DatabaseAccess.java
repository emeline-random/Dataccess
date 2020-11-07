package dataccess.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

@Component
public class DatabaseAccess {

    private DaoConfigurer configurer;
    private Connection connection;

    private Connection getConnection() {
        try {
            this.connection = this.configurer.getConnection();
            return this.connection;
        } catch (SQLException ex) {
            ex.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(Arrays.toString(ex.getStackTrace()), new FacesMessage(ex.getMessage()));
        }
        return null;
    }

    public Statement getStatement() throws DaoAccessException {
        try {
            return this.getConnection().createStatement();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new DaoAccessException(ex);
        }
    }

    public ResultSet execute(String query) throws DaoAccessException {
        try {
            return this.getStatement().executeQuery(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new DaoAccessException(throwables);
        }
    }

    public void closeConnection() throws DaoAccessException {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                throw new DaoAccessException(throwables);
            }
        }
    }

    @Autowired
    public void setConfigurer(DaoConfigurer configurer) {
        this.configurer = configurer;
    }
}
