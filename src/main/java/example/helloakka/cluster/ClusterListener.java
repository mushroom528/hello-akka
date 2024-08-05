package example.helloakka.cluster;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ClusterEvent;

public class ClusterListener extends AbstractBehavior<ClusterEvent.ClusterDomainEvent> {

    private final ClusterGroupManager clusterGroupManager;

    public ClusterListener(ActorContext<ClusterEvent.ClusterDomainEvent> context, ClusterGroupManager clusterGroupManager) {
        super(context);
        this.clusterGroupManager = clusterGroupManager;
    }

    public static Behavior<ClusterEvent.ClusterDomainEvent> create(ClusterGroupManager clusterGroupManager) {
        return Behaviors.setup(ctx -> new ClusterListener(ctx, clusterGroupManager));
    }

    @Override
    public Receive<ClusterEvent.ClusterDomainEvent> createReceive() {
        return newReceiveBuilder()
                .onMessage(ClusterEvent.MemberUp.class, this::onMemberUp)
                .onMessage(ClusterEvent.LeaderChanged.class, this::onLeaderChanged)
                .onMessage(ClusterEvent.MemberRemoved.class, this::onMemberRemoved)
                .onMessage(ClusterEvent.UnreachableMember.class, this::onUnreachableMember)
                .onMessage(ClusterEvent.MemberExited.class, this::onMemberExited)
                .build();
    }

    private Behavior<ClusterEvent.ClusterDomainEvent> onMemberUp(ClusterEvent.MemberUp event) {
        getContext().getLog().info("Member Up: {}", event.member());
        return this;
    }

    private Behavior<ClusterEvent.ClusterDomainEvent> onMemberExited(ClusterEvent.MemberExited event) {
        getContext().getLog().info("Member exited: {}", event.member());
        return this;
    }

    private Behavior<ClusterEvent.ClusterDomainEvent> onLeaderChanged(ClusterEvent.LeaderChanged event) {
        getContext().getLog().info("Leader Changed: {}", event.getLeader());
        return this;
    }

    private Behavior<ClusterEvent.ClusterDomainEvent> onMemberRemoved(ClusterEvent.MemberRemoved event) {
        getContext().getLog().info("Member Removed: {}", event.member());
        if (clusterGroupManager.isLeader()) {
            // node가 graceful이 아닌 비정상적으로 종료되는 경우 다른 노드에서 이벤트를 통해 cluster에서 제거
            String removedAddress = event.member().address().toString();
            clusterGroupManager.leave(removedAddress);
        }
        return this;
    }

    private Behavior<ClusterEvent.ClusterDomainEvent> onUnreachableMember(ClusterEvent.UnreachableMember event) {
        getContext().getLog().info("Member detected as unreachable: {}", event.member().address());
        return this;
    }

}
