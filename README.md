# Dataccess

Web application that allows you to administer an Oracle database or a MySql database. The application allows you to list the schemas (exclusively created users starting with c## for oracle database) and to access the tables they contain. It is possible to perform several operations on tables and databases (creation, deletion, consultation). It is also possible to execute queries directly in sql. 

🔧 In Oracle, schema creation allows to create an "user" with particular rights and a password while in Mysql it allows to create a database that only has a name.

## Configuration

### MySql configuration

To use a Mysql database you need to use the configuration bellow and to change the password and the username in src/main/resources/application.properties. The user must have all privileges on all databases to be able to use all the features of the application. The configuration for oracle needs to be commented.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/?serverTimezone=UTC
spring.datasource.username=username
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```
Then to register the correct spring service you need to check that the annotation ``` @Primary ``` is used at the top of the src/main/java/dataccess/service/MySqlService.java class and that it is not used at the top of the OracleService class.

### Oracle configuration

To use an Oracle database you need to use the configuration bellow and to change the password and the username in src/main/resources/application.properties. To be able to use tha application the user needs to have all privileges on all schemas. The configuration for Mysql needs to be commented.

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XE
spring.datasource.username=username
spring.datasource.password=password
spring.datasource.driver-class-name=oracle.jdbc.driver.OracleDriver
spring.jpa.database-platform=org.hibernate.dialect.Oracle10gDialect
```
Then to register the correct spring service you need to check that the annotation ``` @Primary ``` is used at the top of the src/main/java/dataccess/service/OracleService.java class and that it is not used at the top of the MySqlService class.
