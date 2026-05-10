package pe.com.lacunza.system.restosys.dtos;

import lombok.Data;

@Data
public class BillItemDTO {
    private DishDTO dish;
    private Integer quantity;
    private String notes;
}
