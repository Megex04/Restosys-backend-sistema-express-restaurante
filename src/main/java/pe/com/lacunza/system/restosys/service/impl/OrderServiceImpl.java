package pe.com.lacunza.system.restosys.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.lacunza.system.restosys.dtos.OrderItemRequestDTO;
import pe.com.lacunza.system.restosys.dtos.OrderRequestDTO;
import pe.com.lacunza.system.restosys.entity.Dish;
import pe.com.lacunza.system.restosys.entity.Order;
import pe.com.lacunza.system.restosys.entity.OrderItem;
import pe.com.lacunza.system.restosys.entity.RestaurantTable;
import pe.com.lacunza.system.restosys.repository.DishRepository;
import pe.com.lacunza.system.restosys.repository.OrderRepository;
import pe.com.lacunza.system.restosys.repository.TableRepository;
import pe.com.lacunza.system.restosys.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final TableRepository tableRepository;
    private final DishRepository dishRepository;

    @Transactional
    @Override
    public Order createOrder(OrderRequestDTO request) {
        // 1. Buscar la mesa
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        // 2. Crear el pedido base
        Order order = new Order();
        order.setTable(table);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        // 3. Procesar cada platillo del pedido
        for (var itemRequest : request.getItems()) {
            Dish dish = dishRepository.findById(itemRequest.getDishId())
                    .orElseThrow(() -> new RuntimeException("Platillo no encontrado"));

            if (!dish.getAvailable()) {
                throw new RuntimeException("El platillo " + dish.getName() + " está agotado");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setDish(dish);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(dish.getPrice()); // Congelamos el precio actual
            orderItem.setNotes(itemRequest.getNotes());

            order.getItems().add(orderItem);

            // Sumar al total: precio * cantidad
            BigDecimal itemTotal = dish.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(itemTotal);
        }

        order.setTotalAmount(total);

        // 4. Cambiar el estado de la mesa a OCUPADA
        table.setStatus(RestaurantTable.TableStatus.occupied);
        tableRepository.save(table);

        // 5. Guardar el pedido (CascadeType.ALL guardará los OrderItems automáticamente)
        return orderRepository.save(order);
    }

    @Override
    public Order getActiveOrderByTable(Long tableId) {
        return orderRepository.findByTableIdAndStatus(tableId, Order.OrderStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No hay una orden activa para la mesa " + tableId));
    }

    @Transactional
    @Override
    public Order addItemsToActiveOrder(Long tableId, List<OrderItemRequestDTO> items) {
        Order activeOrder = orderRepository.findByTableIdAndStatus(tableId, Order.OrderStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No hay una orden activa para la mesa " + tableId));

        BigDecimal additionalTotal = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemRequest : items) {
            Dish dish = dishRepository.findById(itemRequest.getDishId())
                    .orElseThrow(() -> new RuntimeException("Platillo no encontrado"));

            if (!dish.getAvailable()) {
                throw new RuntimeException("El platillo " + dish.getName() + " está agotado");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(activeOrder);
            orderItem.setDish(dish);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(dish.getPrice());
            orderItem.setNotes(itemRequest.getNotes());

            activeOrder.getItems().add(orderItem);

            BigDecimal itemTotal = dish.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            additionalTotal = additionalTotal.add(itemTotal);
        }

        BigDecimal currentTotal = activeOrder.getTotalAmount() != null
                ? activeOrder.getTotalAmount()
                : BigDecimal.ZERO;

        activeOrder.setTotalAmount(currentTotal.add(additionalTotal));

        return orderRepository.save(activeOrder);
    }
}
