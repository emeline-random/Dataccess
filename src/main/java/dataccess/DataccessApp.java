package dataccess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import javax.faces.webapp.FacesServlet;

@SpringBootApplication(exclude = { // déclaration de la classe comme classe de configuration
        HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class,
        XADataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class DataccessApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DataccessApp.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DataccessApp.class);
    }

    @Bean //bean permettant d'enregistrer JSF auprès de Spring
    public ServletRegistrationBean<?> servletRegistrationBean() {
        FacesServlet servlet = new FacesServlet();
        return new ServletRegistrationBean<>(servlet, "*.xhtml");
    }

}
