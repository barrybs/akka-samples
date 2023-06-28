package puzzle.actors;

import akka.actor.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.cluster.ddata.*;
import akka.cluster.typed.Cluster;
import puzzle.Piece;
import puzzle.messages.Command;

import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class MainActorOLD extends AbstractBehavior<Command> {

    public static Behavior<Command> create(Integer rows, Integer cols) {
        return Behaviors.setup(context -> new MainActorOLD(context, rows, cols));
    }

    private MainActorOLD(ActorContext<Command> context, Integer rows, Integer cols) {
        super(context);
        Cluster cluster = Cluster.get(context.getSystem());
        SelfUniqueAddress node = DistributedData.get(context.getSystem()).selfUniqueAddress();

        if (cluster.selfMember().hasRole(Roles.distributeddata)){
            final List<Integer> randomPositions = new ArrayList<>();
            IntStream.range(0, rows * cols).forEach(item -> { randomPositions.add(item); });
            Collections.shuffle(randomPositions);
            ActorRef rep = DistributedData.get(context.getSystem()).replicator();

            //Key<ORMap<String, Piece>> puzzleKey = ORMapKey.create("puzzle");
            LWWMap<String, Piece> initialPuzzle = LWWMap.create();
            Integer position=0;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    /*
                    final Image imagePortion = createImage(new FilteredImageSource(image.getSource(),
                            new CropImageFilter(j * imageWidth / columns,
                                    i * imageHeight / rows,
                                    (imageWidth / columns),
                                    imageHeight / rows)));
                    */
                    Piece newPiece = new Piece(position, randomPositions.get(position));
                    initialPuzzle.put(node, position.toString(), newPiece);

                    /*Replicator.Update<ORMap<String, Piece>> update =
                            new Replicator.Update<>(puzzleKey, initialPuzzle, Replicator.writeLocal(), curr -> curr.put(node, pos.toString(), newPiece));

                     */
/*
                    CompletionStage<Replicator.UpdateResponse<ORMap<String, Piece>>> response =
                            AskPattern.ask(
                                    DistributedData.get(getContext().getSystem()).replicator(),
                                    replicator -> update,
                                    Duration.ofSeconds(5),
                                    getContext().getSystem().scheduler()
                            );
*/
                    position++;
                }
            }
            // Salva l'ORMap nel Replicator (writeLocal())
            //replicator.tell(new Replicator.Update<>(puzzleKey, initialPuzzle, Replicator.writeLocal(), map -> map), ActorRef.noSender());
        }

        if (cluster.selfMember().hasRole(Roles.gamerwithgui)){
            //TODO: run PuzzleRender
        }

        if (cluster.selfMember().hasRole(Roles.multisimulator)){
            //TODO: simulator for multiple gamers (only one with GUI lauched with Roles.gamerwithgui)
        }


    }

    @Override
    public Receive<Command> createReceive() {
        return null;
    }
}
