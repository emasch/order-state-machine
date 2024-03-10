package esp.statemachine.services;

import esp.statemachine.statemachine.Events;
import esp.statemachine.statemachine.States;
import org.springframework.statemachine.StateContext;

public interface MailService {

    void sendMailToCustomer(StateContext<States, Events> context);
}
