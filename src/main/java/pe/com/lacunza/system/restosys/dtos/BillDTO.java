package pe.com.lacunza.system.restosys.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BillDTO {
    private Long tableId;
    private List<BillItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
}
