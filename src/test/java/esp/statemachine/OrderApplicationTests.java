package esp.statemachine;

import esp.statemachine.helper.InMemoryStateMachinePersist;
import esp.statemachine.order.Order;
import esp.statemachine.services.MailService;
import esp.statemachine.services.OrderService;
import esp.statemachine.statemachine.Events;
import esp.statemachine.statemachine.StatemachineConfig;
import esp.statemachine.statemachine.States;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

//@SpringBootTest
@ExtendWith(MockitoExtension.class)
class OrderApplicationTests {

    StateMachine<States, Events> buildStateMachine(OrderService orderService, MailService mailService) throws Exception {
        var statemachineConfig = new StatemachineConfig(orderService, mailService);

        StateMachineBuilder.Builder<States, Events> builder = StateMachineBuilder.builder();
        statemachineConfig.configure(builder.configureConfiguration());
        statemachineConfig.configure(builder.configureStates());
        statemachineConfig.configure(builder.configureTransitions());

        return builder.build();
    }

    @Test
    void testMachine() throws Exception {
        var orderServiceMock = mock(OrderService.class);
        int productId = 42;
        when(orderServiceMock.isInStock(productId)).thenReturn(true);

        var mailServiceMock = mock(MailService.class);

        Order order = Order.builder()
                           .id(1)
                           .productId(productId)
                           .build();

        var stateMachine = buildStateMachine(orderServiceMock, mailServiceMock);
var testPlan = StateMachineTestPlanBuilder
    .<States, Events>builder()
    .stateMachine(stateMachine)
    .step()
    .expectState(States.INITIAL)
    .and()
    .step()
    .sendEvent(Mono.just(MessageBuilder
        .withPayload(Events.PLACE_ORDER)
        .setHeader("order", order)
        .build()).block())
    .expectStateChanged(1)
    .expectState(States.PRODUCT_IN_STOCK)
    .and()
    .step()
    .sendEvent(Mono.just(MessageBuilder
        .withPayload(Events.HAND_OVER_TO_SHIPPING_SERVICE_PROVIDER)
        .build()).block())
    .expectStateChanged(1)
    .expectState(States.PRODUCT_SHIPPED)
    .and()
    .build();

testPlan.test();

        verify(orderServiceMock, times(1)).isInStock(productId);
        verify(mailServiceMock, times(1)).sendMailToCustomer(any());

        InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
        StateMachinePersister<States, Events, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

        persister.persist(stateMachine, "676c2e11-2da7-4a6d-a173-81910c74c510");
    }

}
