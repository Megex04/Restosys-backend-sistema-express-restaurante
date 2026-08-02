package pe.com.lacunza.system.restosys.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.lacunza.system.restosys.dtos.BillDTO;
import pe.com.lacunza.system.restosys.dtos.BillItemDTO;
import pe.com.lacunza.system.restosys.dtos.PaymentResponseDTO;
import pe.com.lacunza.system.restosys.entity.Dish;
import pe.com.lacunza.system.restosys.entity.Order;
import pe.com.lacunza.system.restosys.entity.OrderItem;
import pe.com.lacunza.system.restosys.entity.RestaurantTable;
import pe.com.lacunza.system.restosys.repository.OrderRepository;
import pe.com.lacunza.system.restosys.repository.TableRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    private static final Long TABLE_ID = 1L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TableRepository tableRepository;

    @InjectMocks
    private BillingServiceImpl billingServiceImpl;

    private RestaurantTable buildTable(Long id, String number, RestaurantTable.TableStatus status) {
        RestaurantTable table = new RestaurantTable();
        table.setId(id);
        table.setNumber(number);
        table.setCapacity(4);
        table.setStatus(status);
        return table;
    }

    private Dish buildDish(Long id, String name, BigDecimal price) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName(name);
        dish.setPrice(price);
        dish.setAvailable(true);
        return dish;
    }

    private OrderItem buildOrderItem(Dish dish, Integer quantity, String notes) {
        OrderItem item = new OrderItem();
        item.setDish(dish);
        item.setQuantity(quantity);
        item.setUnitPrice(dish.getPrice());
        item.setNotes(notes);
        return item;
    }

    private Order buildOrder(Long id, RestaurantTable table, Order.OrderStatus status, List<OrderItem> items) {
        Order order = new Order();
        order.setId(id);
        order.setTable(table);
        order.setStatus(status);
        order.setItems(items);
        return order;
    }

    // --- generateBillForTable ---

    @Test
    void generateBillForTable_conOrdenPendienteYVariosItems_deberiaCalcularSubtotalImpuestoYTotalCorrectamente() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, "M1", RestaurantTable.TableStatus.occupied);
        Dish lomoSaltado = buildDish(10L, "Lomo Saltado", new BigDecimal("10.00"));
        Dish ceviche = buildDish(20L, "Ceviche", new BigDecimal("15.50"));
        OrderItem item1 = buildOrderItem(lomoSaltado, 3, "Sin cebolla");
        OrderItem item2 = buildOrderItem(ceviche, 2, null);
        Order order = buildOrder(100L, table, Order.OrderStatus.PENDING, List.of(item1, item2));

        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(order));

        // Act
        BillDTO bill = billingServiceImpl.generateBillForTable(TABLE_ID);

        // Assert
        assertEquals(TABLE_ID, bill.getTableId());
        assertEquals(2, bill.getItems().size());

        BillItemDTO billItem1 = bill.getItems().get(0);
        assertEquals("Sin cebolla", billItem1.getNotes());
        assertEquals(3, billItem1.getQuantity());
        assertEquals(lomoSaltado.getId(), billItem1.getDish().getId());
        assertEquals(lomoSaltado.getName(), billItem1.getDish().getName());
        assertEquals(0, lomoSaltado.getPrice().compareTo(billItem1.getDish().getPrice()));

        // subtotal = (10.00 * 3) + (15.50 * 2) = 30.00 + 31.00 = 61.00
        // tax = 61.00 * 0.18 = 10.98
        // total = 61.00 + 10.98 = 71.98
        assertEquals(0, new BigDecimal("61.00").compareTo(bill.getSubtotal()));
        assertEquals(0, new BigDecimal("10.98").compareTo(bill.getTax()));
        assertEquals(0, new BigDecimal("71.98").compareTo(bill.getTotal()));
    }

    @Test
    void generateBillForTable_conOrdenSinItems_deberiaRetornarTotalesEnCero() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, "M1", RestaurantTable.TableStatus.occupied);
        Order order = buildOrder(100L, table, Order.OrderStatus.PENDING, Collections.emptyList());

        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(order));

        // Act
        BillDTO bill = billingServiceImpl.generateBillForTable(TABLE_ID);

        // Assert
        assertTrue(bill.getItems().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(bill.getSubtotal()));
        assertEquals(0, new BigDecimal("0.00").compareTo(bill.getTax()));
        assertEquals(0, new BigDecimal("0.00").compareTo(bill.getTotal()));
    }

    @Test
    void generateBillForTable_deberiaRedondearElImpuestoConHalfUp() {
        // Arrange: subtotal = 11.11 * 3 = 33.33 -> tax = 33.33 * 0.18 = 5.9994 -> redondea a 6.00
        RestaurantTable table = buildTable(TABLE_ID, "M1", RestaurantTable.TableStatus.occupied);
        Dish dish = buildDish(10L, "Menu del dia", new BigDecimal("11.11"));
        OrderItem item = buildOrderItem(dish, 3, null);
        Order order = buildOrder(100L, table, Order.OrderStatus.PENDING, List.of(item));

        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(order));

        // Act
        BillDTO bill = billingServiceImpl.generateBillForTable(TABLE_ID);

        // Assert
        assertEquals(0, new BigDecimal("33.33").compareTo(bill.getSubtotal()));
        assertEquals(0, new BigDecimal("6.00").compareTo(bill.getTax()));
        assertEquals(0, new BigDecimal("39.33").compareTo(bill.getTotal()));
    }

    @Test
    void generateBillForTable_conMesaInexistente_deberiaLanzarRuntimeException() {
        // Arrange
        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> billingServiceImpl.generateBillForTable(TABLE_ID));

        assertEquals("Mesa no encontrada", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    @Test
    void generateBillForTable_conMesaSinOrdenPendiente_deberiaLanzarRuntimeExceptionConDetalle() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, "M5", RestaurantTable.TableStatus.dirty);
        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> billingServiceImpl.generateBillForTable(TABLE_ID));

        assertTrue(exception.getMessage().contains("M5"));
        assertTrue(exception.getMessage().contains("dirty"));
        assertTrue(exception.getMessage().contains("no tiene una orden PENDING asociada"));
    }

    // --- processPayment ---

    @Test
    void processPayment_conOrdenActiva_deberiaMarcarComoPagadaYMesaComoDirty() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, "M1", RestaurantTable.TableStatus.occupied);
        Order order = buildOrder(100L, table, Order.OrderStatus.PENDING, Collections.emptyList());

        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(order));

        // Act
        PaymentResponseDTO response = billingServiceImpl.processPayment(TABLE_ID, "CASH");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Pago procesado exitosamente", response.getMessage());
        assertEquals("CASH", response.getPaymentMethod());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(Order.OrderStatus.PAID, orderCaptor.getValue().getStatus());

        ArgumentCaptor<RestaurantTable> tableCaptor = ArgumentCaptor.forClass(RestaurantTable.class);
        verify(tableRepository).save(tableCaptor.capture());
        assertEquals(RestaurantTable.TableStatus.dirty, tableCaptor.getValue().getStatus());

        verify(tableRepository, times(2)).findById(TABLE_ID);
    }

    @Test
    void processPayment_conMesaInexistente_deberiaLanzarRuntimeExceptionYNoTocarOrden() {
        // Arrange
        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> billingServiceImpl.processPayment(TABLE_ID, "CASH"));

        assertEquals("Mesa no encontrada", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    @Test
    void processPayment_sinOrdenPendiente_deberiaLanzarRuntimeExceptionYNoGuardarNada() {
        // Arrange
        RestaurantTable table = buildTable(TABLE_ID, "M2", RestaurantTable.TableStatus.occupied);
        when(tableRepository.findById(TABLE_ID)).thenReturn(Optional.of(table));
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> billingServiceImpl.processPayment(TABLE_ID, "CASH"));

        verify(orderRepository, never()).save(any());
        verify(tableRepository, never()).save(any());
    }

    @Test
    void processPayment_siLaMesaDesapareceAlActualizarEstado_deberiaLanzarExcepcionLuegoDeGuardarLaOrden() {
        // Arrange: la mesa existe cuando se busca la orden activa, pero ya no cuando
        // el método intenta actualizar su estado a "dirty" (segunda llamada a findById).
        // Esto expone que la orden queda marcada como PAID en el repositorio antes del
        // fallo; en producción @Transactional revertiría ese cambio en la base real,
        // pero el mock no simula rollback.
        RestaurantTable table = buildTable(TABLE_ID, "M1", RestaurantTable.TableStatus.occupied);
        Order order = buildOrder(100L, table, Order.OrderStatus.PENDING, Collections.emptyList());

        when(tableRepository.findById(TABLE_ID))
                .thenReturn(Optional.of(table))
                .thenReturn(Optional.empty());
        when(orderRepository.findByTableIdAndStatus(TABLE_ID, Order.OrderStatus.PENDING)).thenReturn(Optional.of(order));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> billingServiceImpl.processPayment(TABLE_ID, "CASH"));

        assertEquals("Mesa no encontrada", exception.getMessage());
        verify(orderRepository).save(order);
        assertEquals(Order.OrderStatus.PAID, order.getStatus());
        verify(tableRepository, never()).save(any());
    }
}
