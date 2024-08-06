package example.helloakka.actor.distributeddata;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.GCounter;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.SelfUniqueAddress;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.Replicator;
import akka.cluster.ddata.typed.javadsl.ReplicatorMessageAdapter;
import lombok.Getter;

public class Counter extends AbstractBehavior<Counter.Command> {

    private CounterCache cache;

    public interface Command {
    }

    public enum Increment implements Command {
        INSTANCE
    }

    public record GetValue(ActorRef<Integer> replyTo) implements Command {
    }

    public record GetCachedValue(ActorRef<Integer> replyTo) implements Command {
    }

    enum Unsubscribe implements Command {
        INSTANCE
    }

    private interface InternalCommand extends Command {
    }

    private record InternalUpdateResponse(Replicator.UpdateResponse<GCounter> rsp) implements InternalCommand {
    }

    private record InternalGetResponse(Replicator.GetResponse<GCounter> rsp,
                                       ActorRef<Integer> replyTo) implements InternalCommand {
    }

    private record InternalSubscribeResponse(Replicator.SubscribeResponse<GCounter> rsp) implements InternalCommand {
    }

    public static Behavior<Command> create(Key<GCounter> key, CounterCache cache) {
        return Behaviors.setup(
                ctx ->
                        DistributedData.withReplicatorMessageAdapter(
                                (ReplicatorMessageAdapter<Command, GCounter> replicatorAdapter) ->
                                        new Counter(ctx, replicatorAdapter, key, cache)));
    }

    // adapter that turns the response messages from the replicator into our own protocol
    private final ReplicatorMessageAdapter<Command, GCounter> replicatorAdapter;
    private final SelfUniqueAddress node;
    private final Key<GCounter> key;

    @Getter
    private int cachedValue = 0;

    private Counter(
            ActorContext<Command> context,
            ReplicatorMessageAdapter<Command, GCounter> replicatorAdapter,
            Key<GCounter> key, CounterCache cache) {
        super(context);

        this.replicatorAdapter = replicatorAdapter;
        this.key = key;
        this.cache = cache;

        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();
        this.replicatorAdapter.subscribe(this.key, InternalSubscribeResponse::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Increment.class, this::onIncrement)
                .onMessage(InternalUpdateResponse.class, msg -> Behaviors.same())
                .onMessage(GetValue.class, this::onGetValue)
                .onMessage(GetCachedValue.class, this::onGetCachedValue)
                .onMessage(Unsubscribe.class, this::onUnsubscribe)
                .onMessage(InternalGetResponse.class, this::onInternalGetResponse)
                .onMessage(InternalSubscribeResponse.class, this::onInternalSubscribeResponse)
                .build();
    }

    private Behavior<Command> onIncrement(Increment cmd) {
        replicatorAdapter.askUpdate(
                askReplyTo ->
                        new Replicator.Update<>(
                                key,
                                GCounter.empty(),
                                Replicator.writeLocal(),
                                askReplyTo,
                                curr -> curr.increment(node, 1)),
                InternalUpdateResponse::new);

        return this;
    }

    private Behavior<Command> onGetValue(GetValue cmd) {
        replicatorAdapter.askGet(
                askReplyTo -> new Replicator.Get<>(key, Replicator.readLocal(), askReplyTo),
                rsp -> new InternalGetResponse(rsp, cmd.replyTo));

        return this;
    }

    private Behavior<Command> onGetCachedValue(GetCachedValue cmd) {
        cmd.replyTo.tell(cachedValue);
        return this;
    }

    private Behavior<Command> onUnsubscribe(Unsubscribe cmd) {
        replicatorAdapter.unsubscribe(key);
        return this;
    }

    private Behavior<Command> onInternalGetResponse(InternalGetResponse msg) {
        if (msg.rsp instanceof Replicator.GetSuccess) {
            int value = ((Replicator.GetSuccess<?>) msg.rsp).get(key).getValue().intValue();
            msg.replyTo.tell(value);
            getContext().getLog().info("Successfully retrieved value: {}", value);
            return this;
        } else {
            // not dealing with failures
            return Behaviors.unhandled();
        }
    }

    private Behavior<Command> onInternalSubscribeResponse(InternalSubscribeResponse msg) {
        if (msg.rsp instanceof Replicator.Changed) {

            GCounter counter = ((Replicator.Changed<GCounter>) msg.rsp).get(key);
            cachedValue = counter.getValue().intValue();
            cache.cachedValue = counter.getValue().intValue();
            getContext().getLog().info("데이터 변경: {}", cachedValue);
            return this;
        } else {
            // no deletes
            return Behaviors.unhandled();
        }
    }
}
