package pe.com.lacunza.system.restosys.service;

import org.springframework.transaction.annotation.Transactional;
import pe.com.lacunza.system.restosys.dtos.BillDTO;
import pe.com.lacunza.system.restosys.dtos.PaymentResponseDTO;

public interface BillingService {
    BillDTO generateBillForTable(Long tableId);

    @Transactional
    PaymentResponseDTO processPayment(Long tableId, String paymentMethod);
}
