package pe.com.lacunza.system.restosys.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.lacunza.system.restosys.dtos.BillDTO;
import pe.com.lacunza.system.restosys.dtos.PaymentRequestDTO;
import pe.com.lacunza.system.restosys.dtos.PaymentResponseDTO;
import pe.com.lacunza.system.restosys.service.BillingService;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    // GET /api/billing/table/{tableId}
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/table/{tableId}")
    public ResponseEntity<BillDTO> getBillForTable(@PathVariable Long tableId) {
        BillDTO bill = billingService.generateBillForTable(tableId);
        return ResponseEntity.ok(bill);
    }

    // POST /api/billing/table/{tableId}/pay
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    @PostMapping("/table/{tableId}/pay")
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @PathVariable Long tableId,
            @RequestBody PaymentRequestDTO paymentRequest) {

        PaymentResponseDTO response = billingService.processPayment(tableId, paymentRequest.getPaymentMethod());
        return ResponseEntity.ok(response);
    }
}
