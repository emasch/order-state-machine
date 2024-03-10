package esp.statemachine.helper;

import esp.statemachine.statemachine.Events;
import esp.statemachine.statemachine.States;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import java.util.HashMap;
import java.util.Map;

/**
 * @see <a href="https://docs.spring.io/spring-statemachine/docs/current/reference/#sm-persist-statemachinepersister">Persisting State Machines</a>

 */
@Slf4j
public class InMemoryStateMachinePersist implements StateMachinePersist<States, Events, String> {

    private final Map<String, StateMachineContext<States, Events>> contexts = new HashMap<>();

    @Override
    public void write(StateMachineContext<States, Events> context, String contextObj) {
        contexts.put(contextObj, context);
    }

    @Override
    public StateMachineContext<States, Events> read(String contextObj) {
        return contexts.get(contextObj);
    }
}
