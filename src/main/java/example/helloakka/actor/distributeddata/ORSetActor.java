package example.helloakka.actor.distributeddata;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.ORSet;
import akka.cluster.ddata.ORSetKey;
import akka.cluster.ddata.SelfUniqueAddress;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.Replicator;
import akka.cluster.ddata.typed.javadsl.ReplicatorMessageAdapter;

import java.util.Set;

public class ORSetActor extends AbstractBehavior<ORSetActor.Command> {

    public interface Command {
    }

    public interface InternalCommand extends Command {
    }

    record InternalGetResponse(Replicator.GetResponse<ORSet<String>> rsp) implements InternalCommand {
    }

    public record AddElement(String element) implements Command {
    }

    public record RemoveElement(String element) implements Command {
    }

    public record GetElements() implements Command {
    }

    private final SelfUniqueAddress node;
    private final ReplicatorMessageAdapter<Command, ORSet<String>> replicatorAdapter;
    private final ORSetKey<String> dataKey;

    public ORSetActor(ActorContext<Command> context, ReplicatorMessageAdapter<Command, ORSet<String>> replicatorAdapter, ORSetKey<String> dataKey) {
        super(context);
        this.node = DistributedData.get(this.getContext().getSystem()).selfUniqueAddress();
        this.replicatorAdapter = replicatorAdapter;
        this.dataKey = dataKey;
    }

    public static Behavior<Command> create(ORSetKey<String> dataKey) {
        return Behaviors.setup(ctx ->
                DistributedData.withReplicatorMessageAdapter(
                        (ReplicatorMessageAdapter<Command, ORSet<String>> adapter) ->
                                new ORSetActor(ctx, adapter, dataKey)));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddElement.class, this::onAddElement)
                .onMessage(RemoveElement.class, this::onRemoveElement)
                .onMessage(GetElements.class, this::onGetElements)
                .onMessage(InternalGetResponse.class, this::onInternalGetResponse)
                .build();
    }

    private Behavior<Command> onAddElement(AddElement command) {
        replicatorAdapter.askUpdate(
                replyTo -> new Replicator.Update<>(
                        dataKey,
                        ORSet.empty(),
                        Replicator.writeLocal(),
                        replyTo, curr -> curr.add(node, command.element))
                , timeout -> new AddElement(command.element)
        );
        return this;
    }

    private Behavior<Command> onRemoveElement(RemoveElement command) {
        replicatorAdapter.askUpdate(
                replyTo -> new Replicator.Update<>(
                        dataKey,
                        ORSet.empty(),
                        Replicator.writeLocal(),
                        replyTo, curr -> curr.remove(node, command.element))
                , timeout -> new RemoveElement(command.element)
        );
        return this;
    }

    private Behavior<Command> onGetElements(GetElements command) {
        replicatorAdapter.askGet(
                replyTo -> new Replicator.Get<>(dataKey, Replicator.readLocal(), replyTo),
                InternalGetResponse::new
        );
        return this;
    }

    private Behavior<Command> onInternalGetResponse(InternalGetResponse command) {
        if (command.rsp instanceof Replicator.GetSuccess<ORSet<String>> success) {
            ORSet<String> result = success.get(dataKey);
            Set<String> elements = result.getElements();
            getContext().getLog().info("Current elements in the ORSet: " + elements);
            return this;
        }
        return Behaviors.unhandled();
    }
}
