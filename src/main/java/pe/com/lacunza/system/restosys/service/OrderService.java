package pe.com.lacunza.system.restosys.service;

import org.springframework.transaction.annotation.Transactional;
import pe.com.lacunza.system.restosys.dtos.OrderItemRequestDTO;
import pe.com.lacunza.system.restosys.dtos.OrderRequestDTO;
import pe.com.lacunza.system.restosys.entity.Order;

import java.util.List;

public interface OrderService {
    @Transactional
        // Asegura que si algo falla, no se guarde nada a medias en la BD
    Order createOrder(OrderRequestDTO request);
    Order getActiveOrderByTable(Long tableId);
    @Transactional
    Order addItemsToActiveOrder(Long tableId, List<OrderItemRequestDTO> items);
}
