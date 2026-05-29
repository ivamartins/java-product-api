package product.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Spring Boot application.
 *
 * @SpringBootApplication does several things automatically:
 * - Enables auto-configuration
 * - Activates @ComponentScan on the current package and subpackages
 * - Allows using @SpringBootApplication instead of multiple annotations
 *
 * This application demonstrates:
 * - Integration with Kafka (Producer + Consumer)
 * - Usage of PL/pgSQL Stored Procedures via SimpleJdbcCall
 * - Separation between writing (via events) and reading (via JPA)
 */
@SpringBootApplication
public class ProductApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApiApplication.class, args);
    }
}
