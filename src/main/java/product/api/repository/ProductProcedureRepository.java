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
 * Calls PL/pgSQL stored procedures using SimpleJdbcCall.
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

    @PostConstruct
    public void init() {
        // sp_create_product
        createCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_create_product")
                .declareParameters(
                        new SqlParameter("p_name", Types.VARCHAR),
                        new SqlParameter("p_description", Types.VARCHAR),
                        new SqlParameter("p_price", Types.NUMERIC),
                        new SqlParameter("p_id", Types.BIGINT)   // INOUT - returns generated ID
                );

        // sp_update_product
        updateCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_update_product")
                .declareParameters(
                        new SqlParameter("p_id", Types.BIGINT),
                        new SqlParameter("p_name", Types.VARCHAR),
                        new SqlParameter("p_description", Types.VARCHAR),
                        new SqlParameter("p_price", Types.NUMERIC)
                );

        // sp_delete_product
        deleteCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_delete_product")
                .declareParameters(
                        new SqlParameter("p_id", Types.BIGINT)
                );
    }

    /**
     * Calls sp_create_product and returns the generated ID.
     */
    public Long createProduct(String name, String description, BigDecimal price) {
        var params = new MapSqlParameterSource()
                .addValue("p_name", name)
                .addValue("p_description", description)
                .addValue("p_price", price)
                .addValue("p_id", null);

        var result = createCall.execute(params);

        // Spring returns OUT/INOUT parameters in the Map using the original names
        Object id = result.get("p_id");
        return id != null ? ((Number) id).longValue() : null;
    }

    /**
     * Calls sp_update_product.
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
     * Calls sp_delete_product.
     */
    public void deleteProduct(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id", id);

        deleteCall.execute(params);
    }
}
