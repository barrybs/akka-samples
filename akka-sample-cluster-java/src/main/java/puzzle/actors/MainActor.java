package puzzle.actors;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ClusterEvent;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Subscribe;
import pcd.assignment3.puzzle.actors.messages.PuzzleMsg;
import pcd.assignment3.wordcounter.messages.ControlMsg;

public class MainActor extends AbstractBehavior<PuzzleMsg> {
    private MainActor(ActorContext<PuzzleMsg> context) {

        Cluster cluster = Cluster.get(context.getSystem());
    }

    @Override
    public Receive<PuzzleMsg> createReceive() {
        return null;
    }
}
