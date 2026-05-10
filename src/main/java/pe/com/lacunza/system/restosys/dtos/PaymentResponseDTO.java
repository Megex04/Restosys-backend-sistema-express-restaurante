package pe.com.lacunza.system.restosys.dtos;

public class PaymentResponseDTO {
    private boolean success;
    private String message;
    private String paymentMethod;

    // Constructores, Getters y Setters
    public PaymentResponseDTO(boolean success, String message, String paymentMethod) {
        this.success = success;
        this.message = message;
        this.paymentMethod = paymentMethod;
    }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getPaymentMethod() { return paymentMethod; }
}
