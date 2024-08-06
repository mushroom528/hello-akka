package example.helloakka.actor.counter;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import example.helloakka.domain.count.CounterRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CounterActor extends AbstractBehavior<CounterActor.Command> {

    public interface Command {
    }

    public record IncrementCount(Long id, int value) implements Command {
    }

    public static Behavior<Command> create(CounterRepository counterRepository) {
        return Behaviors.setup(ctx -> new CounterActor(ctx, counterRepository));
    }

    private final CounterRepository counterRepository;

    private CounterActor(ActorContext<Command> context, CounterRepository counterRepository) {
        super(context);
        this.counterRepository = counterRepository;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(IncrementCount.class, this::onIncrementCount)
                .build();
    }

    private Behavior<Command> onIncrementCount(IncrementCount command) {
        counterRepository.findById(command.id).ifPresent(counter -> {
            counter.plus(command.value());
            counterRepository.save(counter);
            log.info("Counter incremented: {}/{}", counter.getName(), counter.getNumberValue());
        });
        return this;
    }
}