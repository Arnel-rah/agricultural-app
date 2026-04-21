package hei.school.agriculturalapp.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DatabaseConfig {
    Dotenv  dotenv = Dotenv.load();

    @Bean
    public Connection dataSource() {
       try {
            String url = dotenv.get("DB_URL");
            String username = dotenv.get("DB_USERNAME");
            String password = dotenv.get("DB_PASSWORD");
           assert url != null;
           return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }
}
