package pe.com.lacunza.system.restosys.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.lacunza.system.restosys.dtos.OrderRequestDTO;
import pe.com.lacunza.system.restosys.entity.Order;
import pe.com.lacunza.system.restosys.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequestDTO request) {
        Order newOrder = orderService.createOrder(request);
        return ResponseEntity.ok(newOrder);
    }

    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<Order> getActiveOrderByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(orderService.getActiveOrderByTable(tableId));
    }

    @PostMapping("/table/{tableId}/items")
    public ResponseEntity<Order> addItemsToActiveOrder(
            @PathVariable Long tableId,
            @RequestBody OrderRequestDTO request
    ) {
        Order updatedOrder = orderService.addItemsToActiveOrder(tableId, request.getItems());
        return ResponseEntity.ok(updatedOrder);
    }
}
