package pe.com.lacunza.system.restosys.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.com.lacunza.system.restosys.dtos.OrderItemRequestDTO;
import pe.com.lacunza.system.restosys.dtos.OrderRequestDTO;
import pe.com.lacunza.system.restosys.entity.Order;
import pe.com.lacunza.system.restosys.service.OrderService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void createOrder() {
        // Arrange
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();

        Order newOrder = new Order();
        newOrder.setId(1L);

        when(orderService.createOrder(orderRequestDTO)).thenReturn(newOrder);

        //Act
        ResponseEntity<Order> response = orderController.createOrder(orderRequestDTO);

        // Assert
        assertEquals(newOrder, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getActiveOrderByTable() {
        // Arrange
        Order order = new Order();
        order.setId(1L);

        when(orderService.getActiveOrderByTable(anyLong())).thenReturn(order);

        //Act
        ResponseEntity<Order> response = orderController.getActiveOrderByTable(1L);

        // Assert
        assertEquals(order, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addItemsToActiveOrder() {
        // Arrange
        Order order = new Order();
        order.setId(1L);

        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
        List<OrderItemRequestDTO> items = new ArrayList<>();
        orderRequestDTO.setTableId(25L);
        orderRequestDTO.setItems(items);

        when(orderService.addItemsToActiveOrder(anyLong(), anyList())).thenReturn(order);

        //Act
        ResponseEntity<Order> response = orderController.addItemsToActiveOrder(1L, orderRequestDTO);

        // Assert
        assertEquals(order, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}