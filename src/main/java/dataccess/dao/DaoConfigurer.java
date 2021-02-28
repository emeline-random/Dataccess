package dataccess.dao;

import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Component
@Getter @Setter
public class DaoConfigurer {

    private DataSource source;
    private String username;
    private String password;
    private String driver;
    private String url;

    public DaoConfigurer() { //fixme retirer le path absolu
        try (FileInputStream fis = new FileInputStream("C:\\Users\\mimin\\IdeaProjects\\Dataccess\\src\\main\\resources\\application.properties")) {
            Properties prop = new Properties();
            prop.load(fis);
            this.username = prop.getProperty("spring.datasource.username");
            this.password = prop.getProperty("spring.datasource.password");
            this.driver = prop.getProperty("spring.datasource.driver-class-name");
            this.url = prop.getProperty("spring.datasource.url");
            this.configure();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void configure() {
        if (this.source != null) this.source.close();
        PoolProperties p = new PoolProperties();
        p.setUrl(url);
        p.setDriverClassName(driver);
        p.setUsername(username);
        p.setPassword(password);
        this.source = new DataSource(p);
        this.source.setPoolProperties(p);
    }

    public void setMySqlDatabase(String database) {
        String start = this.url.substring(0, this.url.indexOf("6") + 2);
        String end = this.url.substring(this.url.indexOf("?"));
        this.url = start + database + end;
        this.configure();
    }

    public Connection getConnection() throws SQLException {
        return this.source.getConnection();
    }
}
