package example.helloakka.cluster;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.cluster.ClusterEvent;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Subscribe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClusterSubscriber {

    private final ActorSystem<Void> actorSystem;
    private final Cluster cluster;
    private final ClusterGroupManager clusterGroupManager;

    @PostConstruct
    void init() {
        ActorRef<ClusterEvent.ClusterDomainEvent> clusterListener = actorSystem.systemActorOf(ClusterListener.create(clusterGroupManager), "cluster-listener", Props.empty());
        cluster.subscriptions().tell(Subscribe.create(clusterListener, ClusterEvent.ClusterDomainEvent.class));
    }
}
