package esp.statemachine.services;

public interface OrderService {
    boolean isInStock(int productId);

    boolean isMaterialInStock(int productId);
}
