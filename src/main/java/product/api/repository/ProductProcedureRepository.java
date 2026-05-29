package product.api.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Map;

/**
 * =====================================================================
 * ProductProcedureRepository
 * =====================================================================
 *
 * This class is the **main bridge between Java and PL/pgSQL**.
 *
 * It uses Spring's SimpleJdbcCall to execute stored procedures.
 *
 * Key things to study here:
 * - How to declare and call PostgreSQL procedures from Java
 * - Use of INOUT parameters
 * - @PostConstruct for initialization after dependency injection
 *
 * Full explanation available in STUDY_GUIDE.md → Section 3 (PL/pgSQL)
 */
@Repository
public class ProductProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    // Objetos SimpleJdbcCall pré-configurados (melhor performance)
    private SimpleJdbcCall createCall;
    private SimpleJdbcCall updateCall;
    private SimpleJdbcCall deleteCall;

    public ProductProcedureRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Inicializa os objetos SimpleJdbcCall após a injeção de dependências.
     *
     * @PostConstruct garante que isso rode automaticamente depois que o
     * Spring criar o bean e injetar o JdbcTemplate.
     */
    @PostConstruct
    public void init() {
        // =====================================================
        // PROCEDURE: sp_create_product
        // =====================================================
        // Parâmetros:
        //   p_name        IN  VARCHAR
        //   p_description IN  VARCHAR
        //   p_price       IN  NUMERIC
        //   p_id          INOUT BIGINT   ← retorna o ID gerado pelo banco
        createCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_create_product")
                .declareParameters(
                        new SqlParameter("p_name", Types.VARCHAR),
                        new SqlParameter("p_description", Types.VARCHAR),
                        new SqlParameter("p_price", Types.NUMERIC),
                        new SqlParameter("p_id", Types.BIGINT)   // INOUT
                );

        // =====================================================
        // PROCEDURE: sp_update_product
        // =====================================================
        updateCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_update_product")
                .declareParameters(
                        new SqlParameter("p_id", Types.BIGINT),
                        new SqlParameter("p_name", Types.VARCHAR),
                        new SqlParameter("p_description", Types.VARCHAR),
                        new SqlParameter("p_price", Types.NUMERIC)
                );

        // =====================================================
        // PROCEDURE: sp_delete_product
        // =====================================================
        deleteCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_delete_product")
                .declareParameters(
                        new SqlParameter("p_id", Types.BIGINT)
                );
    }

    /**
     * Executa a stored procedure sp_create_product.
     *
     * @return ID gerado pelo banco de dados (via parâmetro INOUT)
     */
    public Long createProduct(String name, String description, BigDecimal price) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_name", name)
                .addValue("p_description", description)
                .addValue("p_price", price)
                .addValue("p_id", null);   // INOUT - the database will fill this value

        // Executa a procedure
        Map<String, Object> result = createCall.execute(params);

        // O Spring retorna os parâmetros OUT/INOUT no Map com o nome original
        Object id = result.get("p_id");
        return id != null ? ((Number) id).longValue() : null;
    }

    /**
     * Executa a stored procedure sp_update_product.
     */
    public void updateProduct(Long id, String name, String description, BigDecimal price) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id", id)
                .addValue("p_name", name)
                .addValue("p_description", description)
                .addValue("p_price", price);

        updateCall.execute(params);
    }

    /**
     * Executa a stored procedure sp_delete_product.
     */
    public void deleteProduct(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id", id);

        deleteCall.execute(params);
    }
}
