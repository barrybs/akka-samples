package puzzle.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.*;
import akka.cluster.typed.Cluster;
import puzzle.Piece;
import puzzle.messages.Command;
import puzzle.messages.GetMap;
import puzzle.messages.PuzzleMsg;

import java.util.*;
import java.util.stream.IntStream;

public class MainActor extends AbstractBehavior<Command> {

    public static Behavior<Command> create(Integer rows, Integer cols) {
        return Behaviors.setup(context -> new MainActor(context, rows, cols));
    }

    private MainActor(ActorContext<Command> context, Integer rows, Integer cols) {
        super(context);
        Cluster cluster = Cluster.get(context.getSystem());
        Key<LWWMap<String, Piece>> puzzleKey = LWWMapKey.create("puzzle");
        ActorRef<Command> puzzleDD = getContext().spawn(PuzzleLWWMap.create(puzzleKey), "DDActor");

        System.out.println("MainActor.constructor - Roles:"+ cluster.selfMember().getRoles().toString());

        if (cluster.selfMember().hasRole(Roles.distributeddata)) {
            final List<Integer> randomPositions = new ArrayList<>();

            IntStream.range(0, rows * cols).forEach(item -> {
                randomPositions.add(item);
            });
            Collections.shuffle(randomPositions);

            Map<Integer, Integer> testMap = new HashMap<>();
            Piece tempPiece = new Piece(0,0);

            Integer position = 0;
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
                    //Writing the piece on ORMap
                    puzzleDD.tell(new PuzzleLWWMap.UpdatePiece(newPiece.getOriginalPosition().toString(),newPiece));
                    testMap.put(newPiece.getOriginalPosition(), newPiece.getCurrentPosition());

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

            for (Map.Entry<Integer, Integer> pieceEntry : testMap.entrySet()){
                System.out.println("testMap - key string: "+pieceEntry.getKey()+" Value: "+pieceEntry.getValue());

            }

            //TEST: retrieving map content (simulation)
            puzzleDD.tell(new GetMap(context.getSelf()));
            //Pieces exchange simulation
            tempPiece = new Piece(0,testMap.get(0));
            System.out.println("Temp Piece: ori: "+tempPiece.getOriginalPosition()+", current: "+tempPiece.getCurrentPosition());

            Piece p1 = new Piece(0, testMap.get(5));
            System.out.println("p1 - ori: "+p1.getOriginalPosition()+", current: "+p1.getCurrentPosition());
            puzzleDD.tell(new PuzzleLWWMap.UpdatePiece("0", p1));
            puzzleDD.tell(new GetMap(context.getSelf()));

            Piece p2 = new Piece(5, testMap.get(0));
            System.out.println("p2 - ori: "+p2.getOriginalPosition()+", current: "+p2.getCurrentPosition());
            puzzleDD.tell(new PuzzleLWWMap.UpdatePiece("5", p2));

            //TEST: retrieving changed content (simulation)
            puzzleDD.tell(new GetMap(context.getSelf()));
        }

        if (cluster.selfMember().hasRole(Roles.gamerwithgui)) {
            //TODO: run PuzzleRender
        }

        if (cluster.selfMember().hasRole(Roles.multisimulator)) {
            //TODO: simulator for multiple gamers (only one with GUI lauched with Roles.gamerwithgui)
        }


    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                //.onMessage(GetMap.class, this::onGetMap)
                .onMessage(PuzzleMsg.class, this::onPuzzleMsg)
                .build();
    }

    private Behavior<Command> onPuzzleMsg(PuzzleMsg command) {
        System.out.println("onPuzzleMsg - start");
        Map<String, Piece> puzzleMap = command.getMap();
        for (Map.Entry<String, Piece> pieceEntry : puzzleMap.entrySet()){
            System.out.println("Piece - key string: "+pieceEntry.getKey()+" Piece: "
                    +pieceEntry.getValue().getOriginalPosition()+ ", "
                    +pieceEntry.getValue().getCurrentPosition());
        }
        System.out.println("onPuzzleMsg - end");
        return this;
    }

}
