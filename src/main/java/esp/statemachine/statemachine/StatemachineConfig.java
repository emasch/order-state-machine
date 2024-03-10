package esp.statemachine.statemachine;

import esp.statemachine.order.Order;
import esp.statemachine.services.MailService;
import esp.statemachine.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.EnumSet;

@Slf4j
@Component
@Configuration
@RequiredArgsConstructor
@EnableStateMachine(name = "orderStateMachine")
public class StatemachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {
    private final OrderService orderService;
    private final MailService mailService;

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config.withConfiguration()
              .autoStartup(true)
              .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states.withStates()
              .initial(States.INITIAL)
              .end(States.PRODUCT_SHIPPED)
              .junction(States.ORDER_PLACED)
              .state(States.PRODUCT_SHIPPED, mailService::sendMailToCustomer)
              .states(EnumSet.allOf(States.class));
    }

@Override
public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
    transitions
        .withExternal()
        .event(Events.PLACE_ORDER)
        .source(States.INITIAL)
        .target(States.ORDER_PLACED)
    .and()
        .withJunction()
        .source(States.ORDER_PLACED)
        .first(States.PRODUCT_IN_STOCK, isInStock())
        .last(States.PRODUCT_NOT_IN_STOCK)
    .and()
        .withExternal()
        .event(Events.MANUFACTURE_PRODUCT)
        .source(States.PRODUCT_NOT_IN_STOCK)
        .guard(isMaterialInStock())
        .target(States.PRODUCT_MANUFACTURED)
    .and()
        .withExternal()
        .event(Events.PLACE_PRODUCT_IN_STOCK)
        .source(States.PRODUCT_MANUFACTURED)
        .target(States.PRODUCT_IN_STOCK)
    .and()
        .withExternal()
        .event(Events.HAND_OVER_TO_SHIPPING_SERVICE_PROVIDER)
        .source(States.PRODUCT_IN_STOCK)
        .target(States.PRODUCT_SHIPPED);
}

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                if (from == null) {
                    log.info("State changed to {}", to.getId());
                } else {
                    log.info("State changed from {} to {}", from.getId(), to.getId());
                }
            }
        };
    }

    @Bean
    public Guard<States, Events> isMaterialInStock() {
        return context -> {
            var order = context.getExtendedState().get("order", Order.class);

            return orderService.isMaterialInStock(order.getProductId());
        };
    }

    @Bean
    public Guard<States, Events> isInStock() {
        return context -> {
            var msg = context.getMessage();
            Assert.notNull(msg, "Message must not be NULL");

            var order = msg.getHeaders().get("order", Order.class);
            Assert.notNull(order, "Order must not be NULL");

            context.getExtendedState().getVariables().put("order", order);


            return orderService.isInStock(order.getProductId());
        };
    }
}
