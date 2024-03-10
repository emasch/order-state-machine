package esp.statemachine.services;

import esp.statemachine.statemachine.Events;
import esp.statemachine.statemachine.States;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MailServiceImpl implements MailService {

    @Override
    public void sendMailToCustomer(StateContext<States, Events> context) {
        log.info("Mail sent to customer");
    }
}
