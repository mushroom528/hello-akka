package example.helloakka.actor.distributeddata;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.GCounter;
import akka.cluster.ddata.SelfUniqueAddress;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.ReplicatorMessageAdapter;

public class Incrementer extends AbstractBehavior<Incrementer.Command> {

    private final ReplicatorMessageAdapter<Command, GCounter> replicatorAdapter;
    private final SelfUniqueAddress node;

    public interface Command {
    }

    public Incrementer(ActorContext<Command> context, ReplicatorMessageAdapter<Command, GCounter> replicatorAdapter) {
        super(context);
        this.replicatorAdapter = replicatorAdapter;
        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ctx -> DistributedData.withReplicatorMessageAdapter(
                (ReplicatorMessageAdapter<Command, GCounter> replicatorAdapter) ->
                        new Incrementer(ctx, replicatorAdapter)));
    }

    @Override
    public Receive<Command> createReceive() {
        return null;
    }
}
