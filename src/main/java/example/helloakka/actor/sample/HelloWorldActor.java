package example.helloakka.actor.sample;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloWorldActor extends AbstractBehavior<HelloWorldActor.Greet> {

    public record Greet(String message, ActorRef<GreeterActor.SayHello> replyTo) {
    }

    public static Behavior<Greet> create() {
        return Behaviors.setup(HelloWorldActor::new);
    }

    private HelloWorldActor(ActorContext<Greet> context) {
        super(context);
    }

    @Override
    public Receive<Greet> createReceive() {
        return newReceiveBuilder()
                .onMessage(Greet.class, this::onGreet)
                .build();
    }

    private Behavior<Greet> onGreet(Greet command) {
        log.info("command: {}", command.message);
        command.replyTo().tell(new GreeterActor.SayHello("hello world actor!!", getContext().getSelf()));
        return this;
    }
}
