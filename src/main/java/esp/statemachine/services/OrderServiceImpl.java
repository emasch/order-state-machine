package esp.statemachine.services;

import org.springframework.stereotype.Component;

@Component
public class OrderServiceImpl implements OrderService {
    @Override
    public boolean isInStock(int productId) {
        return false;
    }

    @Override
    public boolean isMaterialInStock(int productId) {
        return false;
    }
}
