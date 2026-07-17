package pe.com.lacunza.system.restosys.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.com.lacunza.system.restosys.dtos.BillDTO;
import pe.com.lacunza.system.restosys.dtos.PaymentRequestDTO;
import pe.com.lacunza.system.restosys.dtos.PaymentResponseDTO;
import pe.com.lacunza.system.restosys.service.BillingService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    @Mock
    private BillingService billingService;

    @InjectMocks
    private BillingController billingController;

    @Test
    void getBillForTable() {
        // Arrange
        BillDTO expectedResponse = new BillDTO();
        expectedResponse.setTableId(1L);
        expectedResponse.setTotal(new BigDecimal("100.00"));

        when(billingService.generateBillForTable(anyLong())).thenReturn(expectedResponse);

        //Act
        ResponseEntity<BillDTO> response = billingController.getBillForTable(1L);

        // Assert
        assertEquals(expectedResponse, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void processPayment() {

        // Arrange
        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setPaymentMethod("test");

        PaymentResponseDTO expectedResponse = new PaymentResponseDTO(true, "Exito", "Prestamo");

        when(billingService.processPayment(anyLong(), anyString())).thenReturn(expectedResponse);

        //Act
        ResponseEntity<PaymentResponseDTO> response = billingController.processPayment(1L, requestDTO);

        // Assert
        assertEquals(expectedResponse, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());


    }
}