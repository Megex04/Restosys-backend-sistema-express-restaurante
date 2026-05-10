package pe.com.lacunza.system.restosys.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.lacunza.system.restosys.dtos.BillDTO;
import pe.com.lacunza.system.restosys.dtos.BillItemDTO;
import pe.com.lacunza.system.restosys.dtos.DishDTO;
import pe.com.lacunza.system.restosys.dtos.PaymentResponseDTO;
import pe.com.lacunza.system.restosys.entity.Order;
import pe.com.lacunza.system.restosys.entity.RestaurantTable;
import pe.com.lacunza.system.restosys.repository.OrderRepository;
import pe.com.lacunza.system.restosys.repository.TableRepository;
import pe.com.lacunza.system.restosys.service.BillingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingServiceImpl implements BillingService {

    private final OrderRepository orderRepository;
    private final TableRepository tableRepository;

    public BillingServiceImpl(OrderRepository orderRepository, TableRepository tableRepository) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
    }

    @Override
    public BillDTO generateBillForTable(Long tableId) {
        // 1. Buscar la orden activa (PENDING) para esta mesa
        Order activeOrder = getActiveOrderOrThrow(tableId);

        BigDecimal subtotal = BigDecimal.ZERO;

        // 2. Mapear los items de la base de datos al DTO y calcular subtotal
        List<BillItemDTO> itemDTOs = activeOrder.getItems().stream().map(item -> {
            BillItemDTO dto = new BillItemDTO();
            dto.setQuantity(item.getQuantity());
            dto.setNotes(item.getNotes());

            DishDTO dishDto = new DishDTO();
            dishDto.setId(item.getDish().getId());
            dishDto.setName(item.getDish().getName());
            dishDto.setPrice(item.getDish().getPrice());
            dto.setDish(dishDto);

            return dto;
        }).collect(Collectors.toList());

        for (BillItemDTO item : itemDTOs) {
            BigDecimal itemTotal = item.getDish().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }

        // 3. Calcular impuestos (Ejemplo: 18% de IVA)
        BigDecimal taxRate = new BigDecimal("0.18");
        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        // 4. Armar la respuesta
        BillDTO bill = new BillDTO();
        bill.setTableId(tableId);
        bill.setItems(itemDTOs);
        bill.setSubtotal(subtotal);
        bill.setTax(tax);
        bill.setTotal(total);

        return bill;
    }

    @Transactional
    @Override
    public PaymentResponseDTO processPayment(Long tableId, String paymentMethod) {
        // 1. Buscar la orden activa
        Order activeOrder = getActiveOrderOrThrow(tableId);

        // 2. Marcar la orden como PAGADA (Cerrar la cuenta)
        activeOrder.setStatus(Order.OrderStatus.valueOf("PAID"));
        orderRepository.save(activeOrder);

        // 3. Cambiar el estado de la mesa a "dirty" (sucia) para que el mesero la limpie
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));
        table.setStatus(RestaurantTable.TableStatus.valueOf("dirty"));
        tableRepository.save(table);

        // 4. Retornar éxito a Angular
        return new PaymentResponseDTO(true, "Pago procesado exitosamente", paymentMethod);
    }

    private Order getActiveOrderOrThrow(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        return orderRepository.findByTableIdAndStatus(tableId, Order.OrderStatus.PENDING)
                .orElseThrow(() -> new RuntimeException(
                        "La mesa " + table.getNumber() + " está en estado " + table.getStatus()
                                + " pero no tiene una orden PENDING asociada. "
                                + "Revise la sincronización mesa/orden."
                ));
    }
}
