package example.helloakka.actor.sample;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreeterActor extends AbstractBehavior<GreeterActor.SayHello> {

    public record SayHello(String message, ActorRef<HelloWorldActor.Greet> replyTo) {
    }

    public static Behavior<SayHello> create() {
        return Behaviors.setup(GreeterActor::new);
    }

    private GreeterActor(ActorContext<SayHello> context) {
        super(context);
    }

    @Override
    public Receive<SayHello> createReceive() {
        return newReceiveBuilder()
                .onMessage(SayHello.class, this::onSayHello)
                .build();
    }

    private Behavior<SayHello> onSayHello(SayHello command) {
        log.info("greet {}", command.message);
        command.replyTo.tell(new HelloWorldActor.Greet("Hello, world! from GreeterActor", getContext().getSelf()));
        return this;
    }
}