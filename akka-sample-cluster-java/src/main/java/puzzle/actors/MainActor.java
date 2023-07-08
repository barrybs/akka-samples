package puzzle.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.*;
import akka.cluster.typed.Cluster;
import puzzle.messages.Command;
import puzzle.messages.GetAllMsg;
import puzzle.messages.GetMapMsg;
import puzzle.utils.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class MainActor extends AbstractBehavior<Command> {

    public static Behavior<Command> create(Integer rows, Integer cols, Image image) {
        return Behaviors.setup(context -> new MainActor(context, rows, cols, image));
    }

    private MainActor(ActorContext<Command> context, Integer rows, Integer cols, Image image) {
        super(context);
        Cluster cluster = Cluster.get(context.getSystem());

        if (cluster.selfMember().hasRole(Roles.distributeddata)) {
            final Key<LWWMap<String, Tile>> puzzleKey = LWWMapKey.create("puzzle");
           // final ActorRef<Command> puzzleDD = getContext().spawn(PuzzleLWWMap.create(puzzleKey), "puzzle");
            final List<Integer> randomPositions = new ArrayList<>();

            IntStream.range(0, rows * cols).forEach(item -> {
                randomPositions.add(item);
            });
            Collections.shuffle(randomPositions);

            //Test Map
            Map<Integer, Integer> testMap = new HashMap<>();
            Tile tempPiece = new Tile(null,0,0);

           /* final int imageWidth = image.getWidth(null);
            final int imageHeight = image.getHeight(null);
            Integer position = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                            final Image imagePortion = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(),
                                    new CropImageFilter(j * imageWidth / cols,
                                            i * imageHeight / rows,
                                            (imageWidth / cols),
                                            imageHeight / rows)));
                    Tile tile = new Tile(new ImageIcon(imagePortion), position, randomPositions.get(position));
                    //Writing the piece on LWWMap
                    puzzleDD.tell(new PuzzleLWWMap.UpdatePiece(tile.getOriginalPosition().toString(),tile));
                    testMap.put(tile.getOriginalPosition(), tile.getCurrentPosition());
                    position++;
                }
            }
            puzzleDD.tell(new GetMapMsg(context.getSelf()));
*/

            /*
            for (Map.Entry<Integer, Integer> pieceEntry : testMap.entrySet()){
                System.out.println("testMap - key string: "+pieceEntry.getKey()+" Value: "+pieceEntry.getValue());

            }
            */
            //TEST: retrieving map content (simulation)
            //puzzleDD.tell(new GetMapMsg(context.getSelf()));
            /*

            //Pieces exchange simulation
            tempPiece = new Tile(0,testMap.get(0));
            System.out.println("Temp Piece: ori: "+tempPiece.getOriginalPosition()+", current: "+tempPiece.getCurrentPosition());

            Tile p1 = new Tile(0, testMap.get(5));
            System.out.println("p1 - ori: "+p1.getOriginalPosition()+", current: "+p1.getCurrentPosition());
            puzzleDD.tell(new PuzzleLWWMap.UpdatePiece("0", p1));
            puzzleDD.tell(new GetMapMsg(context.getSelf()));

            Tile p2 = new Tile(5, testMap.get(0));
            System.out.println("p2 - ori: "+p2.getOriginalPosition()+", current: "+p2.getCurrentPosition());
            puzzleDD.tell(new PuzzleLWWMap.UpdatePiece("5", p2));

            //TEST: retrieving changed content (simulation)
            puzzleDD.tell(new GetMap(context.getSelf()));
            */


        }
        //Gamer Actor with GUI.
        /*if (cluster.selfMember().hasRole(Roles.gamerwithgui)) {
            ActorRef<Command> playerActor = getContext().spawn(PlayerActor.create(true), "playerActor");
            PuzzleBoard puzzle = new PuzzleBoard(rows, cols, playerActor);
            puzzle.sendViewToPlayer();
            puzzle.display();
            Log.log("Tell GetMapMsg to itself");
            //playerActor.tell(new GetMapMsg(playerActor));


        }
        if (cluster.selfMember().hasRole(Roles.multisimulator)) {
            //TODO: simulator for multiple gamers (only one with GUI lauched with Roles.gamerwithgui)
        }*/
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                //.onMessage(GetMap.class, this::onGetMap)
                .onMessage(GetAllMsg.class, this::onGetAllMsg)
                .build();
    }

    private Behavior<Command> onGetAllMsg(GetAllMsg command) {
        System.out.println("onGetAllMsg - start");
        Map<String, Piece> puzzleMap = command.getMap();
        for (Map.Entry<String, Piece> tileEntry : puzzleMap.entrySet()){
            System.out.println("Piece - key string: "+tileEntry.getKey()+" Piece: "
                    +tileEntry.getValue().getOriginalPosition()+ ", "
                    +tileEntry.getValue().getCurrentPosition());
        }
        System.out.println("onGetAllMsg - end");
        return this;
    }

}
