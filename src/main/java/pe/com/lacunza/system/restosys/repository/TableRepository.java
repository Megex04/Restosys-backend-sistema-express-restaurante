package pe.com.lacunza.system.restosys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.lacunza.system.restosys.entity.RestaurantTable;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Long> {

}
