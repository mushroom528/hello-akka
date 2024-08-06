package example.helloakka.controller;


import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.cluster.typed.Cluster;
import example.helloakka.actor.distributeddata.Counter;
import example.helloakka.actor.distributeddata.CounterCache;
import example.helloakka.actor.distributeddata.ORSetActor;
import example.helloakka.cluster.AkkaClusterManager;
import example.helloakka.controller.dto.ClusterMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AkkaController {

    private final AkkaClusterManager akkaClusterManager;
    private final ActorRef<Counter.Command> counter;
    private final ActorRef<ORSetActor.Command> orSetActor;
    private final ActorSystem<Void> actorSystem;
    private final Cluster cluster;
    private final CounterCache cache;

    @GetMapping("/members")
    public List<ClusterMember> members() {
        String leader = cluster.state().getLeader().toString();
        return akkaClusterManager.getNodes().stream()
                .map(m -> ClusterMember.of(m.address().toString(), m.status().toString(), leader))
                .toList();
    }

    @GetMapping("/set/add")
    public void setAdd(@RequestParam String text) {
        orSetActor.tell(new ORSetActor.AddElement(text));
    }

    @GetMapping("/set/print")
    public void setPrint() {
        orSetActor.tell(new ORSetActor.GetElements());
    }

    @GetMapping("/increment")
    public void increment() {
        counter.tell(Counter.Increment.INSTANCE);
    }

    @GetMapping("/value")
    public CompletableFuture<Integer> value() {
        CompletionStage<Integer> ask = AskPattern.ask(
                counter,
                Counter.GetValue::new,
                Duration.ofSeconds(3),
                actorSystem.scheduler()
        );
        return ask.toCompletableFuture();
    }

    @GetMapping("/cache/value")
    public CompletableFuture<Integer> cache() {
        CompletionStage<Integer> ask = AskPattern.ask(
                counter,
                Counter.GetCachedValue::new,
                Duration.ofSeconds(3),
                actorSystem.scheduler()
        );
        return ask.toCompletableFuture();
    }

    @GetMapping("/v2/cache/value")
    public int cacheV2() {
        return cache.getCachedValue();
    }

}
