package puzzle.messages;

import akka.actor.typed.ActorRef;

import java.io.Serializable;

public class GetMapMsg implements Command, Serializable {
    public final ActorRef<Command> replyTo;

    public GetMapMsg(ActorRef<Command> replyTo) {
        this.replyTo = replyTo;
    }
}