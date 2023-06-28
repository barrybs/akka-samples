package puzzle.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.LWWMap;
import akka.cluster.ddata.SelfUniqueAddress;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.Replicator;
import akka.cluster.ddata.typed.javadsl.ReplicatorMessageAdapter;
import puzzle.Piece;
import puzzle.messages.Command;
import puzzle.messages.GetMap;
import puzzle.messages.PuzzleMsg;
import puzzle.utils.Log;

import java.time.Duration;
import java.util.Map;

public class PuzzleLWWMap extends AbstractBehavior<Command> {
    private final Replicator.WriteConsistency writeAll = new Replicator.WriteAll(Duration.ofSeconds(5));
    private final Replicator.ReadConsistency readAll = new Replicator.ReadAll(Duration.ofSeconds(3));
    public static class UpdatePiece implements Command {
        public final String key;
        public final Piece piece;

        public UpdatePiece(String key, Piece piece) {
            this.key = key;
            this.piece = piece;
        }

    }
    /*
    public static class GetPiece implements Command {
        public final String key;
        public final ActorRef<Piece> replyTo;

        public GetPiece(String key, ActorRef<Piece> replyTo) {
            this.key = key;Command
            this.replyTo = replyTo;
        }
    }
    */
/*    public class GetMap implements Command {
        public final ActorRef<PuzzleMsg> replyTo;

        public GetMap(ActorRef<PuzzleMsg> replyTo) {
            this.replyTo = replyTo;
        }
    }
*/
    /*public static class GetCachedPiece implements Command {
        public final String key;
        public final ActorRef<Piece> replyTo;

        public GetCachedPiece(String key, ActorRef<Piece> replyTo) {
            this.key = key;
            this.replyTo = replyTo;
        }
    }*/

    public static class GetCachedMap implements Command {
        public final ActorRef<Command> replyTo;

        public GetCachedMap(ActorRef<Command> replyTo) {
            this.replyTo = replyTo;
        }
    }

    enum Unsubscribe implements Command {
        INSTANCE
    }

    private interface InternalCommand extends Command {
    }

    private static class InternalUpdateResponse implements InternalCommand {
        final Replicator.UpdateResponse<LWWMap<String, Piece>> rsp;

        InternalUpdateResponse(Replicator.UpdateResponse<LWWMap<String, Piece>> rsp) {
            this.rsp = rsp;
        }
    }
/*
    private static class InternalGetResponse implements InternalCommand {
        final Replicator.GetResponse<Piece> rsp;
        final ActorRef<Piece> replyTo;
        final String key;

        InternalGetResponse(Replicator.GetResponse<Piece> rsp, String key, ActorRef<Piece> replyTo) {
            this.rsp = rsp;
            this.replyTo = replyTo;
            this.key = key;
        }
    }
*/
    private static class InternalGetMapResponse implements InternalCommand {
        final Replicator.GetResponse<LWWMap<String, Piece>> rsp;
        final ActorRef<Command> replyTo;

        InternalGetMapResponse(Replicator.GetResponse<LWWMap<String, Piece>> rsp, ActorRef<Command> replyTo) {
            this.rsp = rsp;
            this.replyTo = replyTo;
        }
    }

    private static final class InternalSubscribeResponse implements InternalCommand {
        final Replicator.SubscribeResponse<LWWMap<String, Piece>> rsp;

        InternalSubscribeResponse(Replicator.SubscribeResponse<LWWMap<String, Piece>> rsp) {
            this.rsp = rsp;
        }
    }

    public static Behavior<Command> create(Key<LWWMap<String, Piece>> key) {
        return Behaviors.setup(
                ctx ->
                        DistributedData.withReplicatorMessageAdapter(
                                (ReplicatorMessageAdapter<Command, LWWMap<String, Piece>> replicatorAdapter) ->
                                        new PuzzleLWWMap(ctx, replicatorAdapter, key)));
    }

    // adapter that turns the response messages from the replicator into our own protocol
    private final ReplicatorMessageAdapter<Command, LWWMap<String, Piece>> replicatorAdapter;
    private final SelfUniqueAddress node;
    private final Key<LWWMap<String, Piece>> key;
    private Piece cachedPiece;
    private Map<String, Piece> cachedMap;


    private PuzzleLWWMap(
            ActorContext<Command> context,
            ReplicatorMessageAdapter<Command, LWWMap<String, Piece>> replicatorAdapter,
            Key<LWWMap<String, Piece>> key) {

        super(context);
        this.replicatorAdapter = replicatorAdapter;
        this.key = key;
        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();
        this.replicatorAdapter.subscribe(this.key, InternalSubscribeResponse::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(UpdatePiece.class, this::onUpdatePiece)
                .onMessage(InternalUpdateResponse.class, msg -> Behaviors.same())
                //.onMessage(GetPiece.class, this::onGetPiece)
                //.onMessage(GetCachedPiece.class, this::onGetCachedPiece)
                .onMessage(GetMap.class, this::onGetMap)
                .onMessage(GetCachedMap.class, this::onGetCachedMap)
                .onMessage(Unsubscribe.class, this::onUnsubscribe)
                //.onMessage(InternalGetResponse.class, this::onInternalGetResponse)
                .onMessage(InternalGetMapResponse.class, this::onInternalGetMapResponse)
                .onMessage(InternalSubscribeResponse.class, this::onInternalSubscribeResponse)
                .build();
    }


    //WriteAll
    /*
    private Behavior<Command> onUpdatePiece(UpdatePiece cmd) {
        replicatorAdapter.askUpdate(
                askReplyTo ->
                        new Replicator.Update<>(
                                key,
                                LWWMap.empty(),
                                writeAll,
                                askReplyTo,
                                curr -> curr.put(node, cmd.key, cmd.piece)),
                InternalUpdateResponse::new);
        Log.log("onUpdatePiece executed");
        return this;
    }
    */

    /*
    private Behavior<Command> onUpdatePiece(UpdatePiece cmd) {

        replicatorAdapter.askUpdate(
                askReplyTo -> new Replicator.Update<LWWMap<String,Piece>>(key, LWWMap.<String, Piece>empty(), askReplyTo, curr -> curr.put(node, cmd.key, cmd.piece)),
                InternalUpdateResponse::new);

     */

        /*
        replicatorAdapter.askUpdate(
                askReplyTo ->
                        new Replicator.WriteAll<>(key, LWWMap.<String, Piece>empty(), askReplyTo, curr -> curr.put(node, cmd.key, cmd.piece))
                ,
                InternalUpdateResponse::new);

        return this;
    }  */
    //writeLocal

    private Behavior<Command> onUpdatePiece(UpdatePiece cmd) {
        replicatorAdapter.askUpdate(
                askReplyTo ->
                        new Replicator.Update<>(
                                key,
                                LWWMap.empty(),
                                Replicator.writeLocal(),
                                askReplyTo,
                                curr -> curr.put(node, cmd.key, cmd.piece)),
                InternalUpdateResponse::new);

        return this;
    }

    /*
    private Behavior<Command> onGetPiece(GetPiece cmd) {
        replicatorAdapter.askGet(
                askReplyTo -> new Replicator.Get<>(key, Replicator.readLocal(), askReplyTo),
                rsp -> new InternalGetResponse(rsp, cmd.key, cmd.replyTo));
        return this;
    }*/

    /*
    //readAll
    private Behavior<Command> onGetMap(GetMap cmd) {
        replicatorAdapter.askGet(
                askReplyTo -> new Replicator.Get<>(key, readAll, askReplyTo),
                rsp -> new InternalGetMapResponse(rsp, cmd.replyTo));

        Log.log("onGetMap executed");
        return this;
    }
    */

    //readLocal

    private Behavior<Command> onGetMap(GetMap cmd) {

        replicatorAdapter.askGet(
                askReplyTo -> new Replicator.Get<>(key, Replicator.readLocal(), askReplyTo),
                rsp -> new InternalGetMapResponse(rsp, cmd.replyTo));

        Log.log("onGetMap executed");
        return this;
    }


    /*
    private Behavior<Command> onGetCachedPiece(GetCachedPiece cmd) {
        cmd.replyTo.tell(cachedPiece);
        return this;
    }
    */
    private Behavior<Command> onGetCachedMap(GetCachedMap cmd) {
        cmd.replyTo.tell(new PuzzleMsg(cachedMap));
        return this;
    }

    private Behavior<Command> onUnsubscribe(Unsubscribe cmd) {
        replicatorAdapter.unsubscribe(key);
        return this;
    }

    /*
    private Behavior<Command> onInternalGetResponse(InternalGetResponse msg) {
        if (msg.rsp instanceof Replicator.GetSuccess) {
            Piece value = ((Replicator.GetSuccess<?>) msg.rsp).get(key).get(msg.key);
            msg.replyTo.tell(value);
            return this;
        } else {
            // not dealing with failures
            return Behaviors.unhandled();
        }
    }
    */

    private Behavior<Command> onInternalGetMapResponse(InternalGetMapResponse msg) {
        if (msg.rsp instanceof Replicator.GetSuccess) {
            Map<String, Piece> value = ((Replicator.GetSuccess<?>) msg.rsp).get(key).getEntries();
            msg.replyTo.tell(new PuzzleMsg(value));
            return this;
        } else {
            // not dealing with failures
            return Behaviors.unhandled();
        }
    }

    private Behavior<Command> onInternalSubscribeResponse(InternalSubscribeResponse msg) {
        if (msg.rsp instanceof Replicator.Changed) {
            LWWMap map = ((Replicator.Changed<?>) msg.rsp).get(key);
            cachedMap = map.getEntries();
            return this;
        } else {
            // no deletes
            return Behaviors.unhandled();
        }
    }


}

