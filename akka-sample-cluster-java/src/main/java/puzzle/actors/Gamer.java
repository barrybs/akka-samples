package puzzle.actors;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import puzzle.messages.Command;

public class Gamer extends AbstractBehavior<Command> {
    public Gamer( ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return null;
    }
}


