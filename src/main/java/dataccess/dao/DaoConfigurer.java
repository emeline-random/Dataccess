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

    private DataSource source;

    public DaoConfigurer() {
        try (FileInputStream fis = new FileInputStream("target/classes/application.properties")) {
            Properties prop = new Properties();
            prop.load(fis);
            String username = prop.getProperty("spring.datasource.username");
            String password = prop.getProperty("spring.datasource.password");
            String driver = prop.getProperty("spring.datasource.driver-class-name");
            String url = prop.getProperty("spring.datasource.url");
            PoolProperties p = new PoolProperties();
            p.setUrl(url);
            p.setDriverClassName(driver);
            p.setUsername(username);
            p.setPassword(password);
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
}
