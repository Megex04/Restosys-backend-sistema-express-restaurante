package pe.com.lacunza.system.restosys.dtos;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    private Long tableId;
    private List<OrderItemRequestDTO> items;
}
