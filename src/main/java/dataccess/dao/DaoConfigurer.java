package dataccess.dao;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Component
public class DaoConfigurer {

    private String username;
    private String password;
    private String driver;
    private String url;
    private DataSource source;

    public DaoConfigurer() {
        try (FileInputStream fis = new FileInputStream("application.properties")) {
            Properties prop = new Properties();
            prop.load(fis);
            this.username = prop.getProperty("spring.datasource.username");
            this.password = prop.getProperty("spring.datasource.password");
            this.driver = prop.getProperty("spring.datasource.driver-class-name");
            this.url = prop.getProperty("spring.datasource.url");
            PoolProperties p = new PoolProperties();
            p.setUrl(this.url);
            p.setDriverClassName(this.driver);
            p.setUsername(this.username);
            p.setPassword(this.password);
            this.source = new org.apache.tomcat.jdbc.pool.DataSource(p);
            this.source.setPoolProperties(p);
            System.out.println("la source est configurée");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return this.source.getConnection();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
