package esp.statemachine;

import esp.statemachine.order.Order;
import esp.statemachine.statemachine.Events;
import esp.statemachine.statemachine.States;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootApplication
public class OrderApplication {

    @Autowired
    private StateMachine<States, Events> stateMachine;

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        log.warn("I am running");
    }

    @PostConstruct
    private void foo() {
        var order = Order
                .builder()
                .id(1)
                .productId(42)
                .build();

        stateMachine
                .sendEvent(Mono
                        .just(MessageBuilder
                                .withPayload(Events.PLACE_ORDER)
                                .setHeader("order", order)
                                .build()))
                .subscribe();
    }
}
