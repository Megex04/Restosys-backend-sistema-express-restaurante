package pe.com.lacunza.system.restosys.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.lacunza.system.restosys.dtos.OrderItemRequestDTO;
import pe.com.lacunza.system.restosys.dtos.OrderRequestDTO;
import pe.com.lacunza.system.restosys.entity.Dish;
import pe.com.lacunza.system.restosys.entity.Order;
import pe.com.lacunza.system.restosys.entity.OrderItem;
import pe.com.lacunza.system.restosys.entity.RestaurantTable;
import pe.com.lacunza.system.restosys.repository.DishRepository;
import pe.com.lacunza.system.restosys.repository.OrderRepository;
import pe.com.lacunza.system.restosys.repository.TableRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Long TABLE_ID = 1L;
    private static final Long DISH_ID = 10L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    private RestaurantTable buildTable(Long id, RestaurantTable.TableStatus status) {
        RestaurantTable table = new RestaurantTable();
        table.setId(id);
        table.setNumber("M1");
        table.setCapacity(4);
        table.setStatus(status);
        return table;
    }

    private Dish buildDish(Long id, String name, BigDecimal price, boolean available) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName(name);
        dish.setPrice(price);
        dish.setAvailable(available);
        return dish;
    }

    private OrderItemRequestDTO buildItemRequest(Long dishId, Integer quantity, String notes) {
        OrderItemRequestDTO dto = new OrderItemRequestDTO();
        dto.setDishId(dishId);
        dto.setQuantity(quantity);
        dto.setNotes(notes);
        return dto;
    }

    private OrderRequestDTO buildOrderRequest(Long tableId, List<OrderItemRequestDTO> items) {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setTableId(tableId);
        dto.setItems(items);
        return dto;
    }

    private Order buildActiveOrder(Long id, BigDecimal totalAmount) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setItems(new ArrayList<>());
        return order;
    }

    // --- createOrder ---

    @Test
    void createOrder_conVariosItemsValidos_deberiaCrearOrdenYOcuparLaMesa() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, RestaurantTable.TableStatus.available);
        Dish lomoSaltado = buildDish(10L, "Lomo Saltado", new BigDecimal("10.00"), true);
        Dish ceviche = buildDish(20L, "Ceviche", new BigDecimal("15.50"), true);

        OrderRequestDTO request = buildOrderRequest(TABLE_ID, List.of(
                buildItemRequest(10L, 2, "Sin cebolla"),
                buildItemRequest(20L, 1, null)
        ));

        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(dishRepository.findById(10L)).thenReturn(Optional.of(lomoSaltado));
        when(dishRepository.findById(20L)).thenReturn(Optional.of(ceviche));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order resultado = orderServiceImpl.createOrder(request);

        // Assert
        assertEquals(Order.OrderStatus.PENDING, resultado.getStatus());
        assertEquals(table, resultado.getTable());
        assertNotNull(resultado.getCreatedAt());
        assertEquals(2, resultado.getItems().size());

        OrderItem item1 = resultado.getItems().get(0);
        assertEquals(lomoSaltado, item1.getDish());
        assertEquals(2, item1.getQuantity());
        assertEquals(0, lomoSaltado.getPrice().compareTo(item1.getUnitPrice()));
        assertEquals("Sin cebolla", item1.getNotes());
        assertEquals(resultado, item1.getOrder());

        // total = (10.00 * 2) + (15.50 * 1) = 20.00 + 15.50 = 35.50
        assertEquals(0, new BigDecimal("35.50").compareTo(resultado.getTotalAmount()));

        assertEquals(RestaurantTable.TableStatus.occupied, table.getStatus());
        verify(tableRepository).save(table);
        verify(orderRepository).save(resultado);
    }

    @Test
    void createOrder_deberiaRetornarElObjetoQueDevuelveElRepositorio() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, RestaurantTable.TableStatus.available);
        OrderRequestDTO request = buildOrderRequest(TABLE_ID, Collections.emptyList());
        Order ordenPersistida = buildActiveOrder(999L, BigDecimal.ZERO);

        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(orderRepository.save(any(Order.class))).thenReturn(ordenPersistida);

        // Act
        Order resultado = orderServiceImpl.createOrder(request);

        // Assert
        assertSame(ordenPersistida, resultado);
    }

    @Test
    void createOrder_conListaDeItemsVacia_deberiaCrearOrdenConTotalCero() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, RestaurantTable.TableStatus.available);
        OrderRequestDTO request = buildOrderRequest(TABLE_ID, Collections.emptyList());

        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order resultado = orderServiceImpl.createOrder(request);

        // Assert
        assertTrue(resultado.getItems().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.getTotalAmount()));
        assertEquals(RestaurantTable.TableStatus.occupied, table.getStatus());
        verify(tableRepository).save(table);
        verifyNoInteractions(dishRepository);
    }

    @Test
    void createOrder_conMesaInexistente_deberiaLanzarRuntimeExceptionYNoCrearNada() {
        // Arrange
        OrderRequestDTO request = buildOrderRequest(TABLE_ID, List.of(buildItemRequest(DISH_ID, 1, null)));
        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderServiceImpl.createOrder(request));

        assertEquals("Mesa no encontrada", exception.getMessage());
        verifyNoInteractions(dishRepository, orderRepository);
        verify(tableRepository, never()).save(any());
    }

    @Test
    void createOrder_conPlatilloInexistente_deberiaLanzarRuntimeExceptionYNoPersistirNada() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, RestaurantTable.TableStatus.available);
        OrderRequestDTO request = buildOrderRequest(TABLE_ID, List.of(buildItemRequest(DISH_ID, 1, null)));

        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(dishRepository.findById(DISH_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderServiceImpl.createOrder(request));

        assertEquals("Platillo no encontrado", exception.getMessage());
        verify(tableRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_conPlatilloAgotado_deberiaLanzarRuntimeExceptionConNombreDelPlatillo() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, RestaurantTable.TableStatus.available);
        Dish dishAgotado = buildDish(DISH_ID, "Aji de Gallina", new BigDecimal("12.00"), false);
        OrderRequestDTO request = buildOrderRequest(TABLE_ID, List.of(buildItemRequest(DISH_ID, 1, null)));

        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dishAgotado));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderServiceImpl.createOrder(request));

        assertEquals("El platillo Aji de Gallina está agotado", exception.getMessage());
        verify(tableRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // --- getActiveOrderByTable ---

    @Test
    void getActiveOrderByTable_conOrdenActiva_deberiaRetornarla() {
        // Arrange
        Order order = buildActiveOrder(100L, new BigDecimal("50.00"));
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(order));

        // Act
        Order resultado = orderServiceImpl.getActiveOrderByTable(TABLE_ID);

        // Assert
        assertSame(order, resultado);
    }

    @Test
    void getActiveOrderByTable_sinOrdenActiva_deberiaLanzarRuntimeException() {
        // Arrange
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderServiceImpl.getActiveOrderByTable(TABLE_ID));

        assertEquals("No hay una orden activa para la mesa " + TABLE_ID, exception.getMessage());
    }

    // --- addItemsToActiveOrder ---

    @Test
    void addItemsToActiveOrder_conOrdenActivaExistente_deberiaSumarAlTotalExistente() {
        // Arrange
        Order activeOrder = buildActiveOrder(100L, new BigDecimal("20.00"));
        Dish dish = buildDish(DISH_ID, "Chicha morada", new BigDecimal("5.00"), true);
        List<OrderItemRequestDTO> nuevosItems = List.of(buildItemRequest(DISH_ID, 2, "Bien fria"));

        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(activeOrder));
        when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(orderRepository.save(activeOrder)).thenReturn(activeOrder);

        // Act
        Order resultado = orderServiceImpl.addItemsToActiveOrder(TABLE_ID, nuevosItems);

        // Assert
        assertEquals(1, resultado.getItems().size());
        OrderItem itemAgregado = resultado.getItems().get(0);
        assertEquals(dish, itemAgregado.getDish());
        assertEquals(2, itemAgregado.getQuantity());
        assertEquals("Bien fria", itemAgregado.getNotes());
        assertEquals(activeOrder, itemAgregado.getOrder());

        // total = 20.00 (existente) + (5.00 * 2) = 30.00
        assertEquals(0, new BigDecimal("30.00").compareTo(resultado.getTotalAmount()));
        verify(orderRepository).save(activeOrder);
    }

    @Test
    void addItemsToActiveOrder_conTotalAmountNuloEnLaOrden_deberiaPartirDeCero() {
        // Arrange
        Order activeOrder = buildActiveOrder(100L, null);
        Dish dish = buildDish(DISH_ID, "Inca Kola", new BigDecimal("4.00"), true);
        List<OrderItemRequestDTO> nuevosItems = List.of(buildItemRequest(DISH_ID, 1, null));

        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(activeOrder));
        when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(orderRepository.save(activeOrder)).thenReturn(activeOrder);

        // Act
        Order resultado = orderServiceImpl.addItemsToActiveOrder(TABLE_ID, nuevosItems);

        // Assert
        assertEquals(0, new BigDecimal("4.00").compareTo(resultado.getTotalAmount()));
    }

    @Test
    void addItemsToActiveOrder_sinOrdenActiva_deberiaLanzarRuntimeExceptionYNoConsultarPlatillos() {
        // Arrange
        List<OrderItemRequestDTO> nuevosItems = List.of(buildItemRequest(DISH_ID, 1, null));
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderServiceImpl.addItemsToActiveOrder(TABLE_ID, nuevosItems));

        assertEquals("No hay una orden activa para la mesa " + TABLE_ID, exception.getMessage());
        verifyNoInteractions(dishRepository);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void addItemsToActiveOrder_conPlatilloInexistente_deberiaLanzarRuntimeExceptionYNoGuardar() {
        // Arrange
        Order activeOrder = buildActiveOrder(100L, BigDecimal.ZERO);
        List<OrderItemRequestDTO> nuevosItems = List.of(buildItemRequest(DISH_ID, 1, null));

        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(activeOrder));
        when(dishRepository.findById(DISH_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderServiceImpl.addItemsToActiveOrder(TABLE_ID, nuevosItems));

        assertEquals("Platillo no encontrado", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void addItemsToActiveOrder_conPlatilloAgotado_deberiaLanzarRuntimeExceptionConNombreDelPlatillo() {
        // Arrange
        Order activeOrder = buildActiveOrder(100L, BigDecimal.ZERO);
        Dish dishAgotado = buildDish(DISH_ID, "Anticucho", new BigDecimal("8.00"), false);
        List<OrderItemRequestDTO> nuevosItems = List.of(buildItemRequest(DISH_ID, 1, null));

        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(activeOrder));
        when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dishAgotado));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderServiceImpl.addItemsToActiveOrder(TABLE_ID, nuevosItems));

        assertEquals("El platillo Anticucho está agotado", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void addItemsToActiveOrder_conListaDeItemsVacia_deberiaGuardarSinModificarElTotal() {
        // Arrange
        Order activeOrder = buildActiveOrder(100L, new BigDecimal("15.00"));

        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(activeOrder));
        when(orderRepository.save(activeOrder)).thenReturn(activeOrder);

        // Act
        Order resultado = orderServiceImpl.addItemsToActiveOrder(TABLE_ID, Collections.emptyList());

        // Assert
        assertTrue(resultado.getItems().isEmpty());
        assertEquals(0, new BigDecimal("15.00").compareTo(resultado.getTotalAmount()));
        verifyNoInteractions(dishRepository);
        verify(orderRepository).save(activeOrder);
    }
}
