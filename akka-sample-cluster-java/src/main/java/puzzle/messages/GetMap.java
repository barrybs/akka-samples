package puzzle.messages;

import akka.actor.typed.ActorRef;

public class GetMap implements Command {
    public final ActorRef<Command> replyTo;

    public GetMap(ActorRef<Command> replyTo) {
        this.replyTo = replyTo;
    }
}