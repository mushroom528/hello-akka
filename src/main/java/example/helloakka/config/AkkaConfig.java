package example.helloakka.config;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.ddata.GCounterKey;
import akka.cluster.ddata.ORSetKey;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.helloakka.actor.counter.CounterActor;
import example.helloakka.actor.distributeddata.Counter;
import example.helloakka.actor.distributeddata.ORSetActor;
import example.helloakka.domain.count.CounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AkkaConfig {

    private final CounterRepository counterRepository;

    @Bean
    public ActorSystem<Void> actorSystem() {
        Config config = ConfigFactory.load();
        return ActorSystem.create(Behaviors.empty(), "akka-system", config);
    }

    @Bean
    public Cluster cluster() {
        return Cluster.get(actorSystem());
    }

    @Bean
    public ActorRef<CounterActor.Command> counterActor() {
        return actorSystem().systemActorOf(CounterActor.create(counterRepository), "counterActor", Props.empty());
    }

    @Bean
    public ActorRef<Counter.Command> distributedCounterActor() {
        return actorSystem().systemActorOf(Counter.create(GCounterKey.create("test")), "distribute-counter", Props.empty());
    }

    @Bean
    public ActorRef<ORSetActor.Command> distributedORSetActor() {
        ORSetKey<String> dataKey = new ORSetKey<>("hello");
        return actorSystem().systemActorOf(ORSetActor.create(dataKey), "distribute-orset", Props.empty());
    }


}
