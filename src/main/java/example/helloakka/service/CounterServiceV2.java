package example.helloakka.service;

import akka.actor.typed.ActorRef;
import example.helloakka.actor.counter.CounterActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CounterServiceV2 {

    private final ActorRef<CounterActor.Command> counterActor;

    @Transactional
    public void plus(Long id, int value) {
        counterActor.tell(new CounterActor.IncrementCounter(id, value));
    }
}
