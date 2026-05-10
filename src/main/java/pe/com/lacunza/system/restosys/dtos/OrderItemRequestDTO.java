package pe.com.lacunza.system.restosys.dtos;

import lombok.Data;

@Data
public class OrderItemRequestDTO {
    private Long dishId;
    private Integer quantity;
    private String notes;
}
