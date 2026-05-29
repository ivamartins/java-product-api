package product.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import product.api.entity.Product;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {
}
