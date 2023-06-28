package puzzle.actors;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.DistributedData;
import akka.cluster.ddata.SelfUniqueAddress;
import akka.cluster.typed.Cluster;
import puzzle.messages.Command;

public class PuzzleRender extends AbstractBehavior<Command> {

    public PuzzleRender(ActorContext<Command> context) {
        super(context);
    }

    /*public PuzzleRender(ActorContext<Command> context, Integer rows, Integer columns, String imagePath){
            Cluster cluster = Cluster.get(context.getSystem());
            SelfUniqueAddress node = DistributedData.get(context.getSystem()).selfUniqueAddress();

            PuzzleBoard puzzleBoard = new PuzzleBoard(rows, columns, imagePath);
            puzzleBoard.setVisible(true);
        }*/
    @Override
    public Receive<Command> createReceive() {
        return null;
    }
}
