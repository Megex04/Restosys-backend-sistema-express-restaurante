package pe.com.lacunza.system.restosys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.lacunza.system.restosys.entity.Dish;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    // Spring genera el SQL: SELECT * FROM dishes WHERE category_id = ?
    List<Dish> findByCategoryId(Long categoryId);

    // Spring genera el SQL: SELECT * FROM dishes WHERE available = true
    List<Dish> findByAvailableTrue();
}
