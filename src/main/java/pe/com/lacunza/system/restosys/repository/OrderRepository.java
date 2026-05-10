package pe.com.lacunza.system.restosys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.lacunza.system.restosys.entity.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Buscar pedidos activos de una mesa
    List<Order> findByTableIdAndStatusNot(Long tableId, Order.OrderStatus status);
    Optional<Order> findByTableIdAndStatus(Long tableId, Order.OrderStatus status);
}
