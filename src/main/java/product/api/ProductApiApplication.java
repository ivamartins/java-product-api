package product.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação Spring Boot.
 *
 * @SpringBootApplication faz várias coisas automaticamente:
 * - Habilita auto-configuration
 * - Ativa @ComponentScan no pacote atual e subpacotes
 * - Permite usar @SpringBootApplication em vez de várias anotações
 *
 * Esta aplicação demonstra:
 * - Integração com Kafka (Producer + Consumer)
 * - Uso de Stored Procedures PL/pgSQL via SimpleJdbcCall
 * - Separação entre escrita (via eventos) e leitura (via JPA)
 */
@SpringBootApplication
public class ProductApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApiApplication.class, args);
    }
}
