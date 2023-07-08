package puzzle.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import puzzle.messages.*;
import puzzle.utils.Log;
import sample.cluster.transformation.Frontend;
import sample.cluster.transformation.Worker;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PlayerActor extends AbstractBehavior<Command> {
    private final Boolean isGuiOn;
    private PuzzleBoard puzzleBoard;
    private Optional<ActorRef<Command>> puzzleDD;
    private BufferedImage image;
    private Integer rows;
    private Integer cols;


    public static final class ListingResponse implements Command {
        final Receptionist.Listing listing;
        public ListingResponse(Receptionist.Listing listing) {
            this.listing = listing;
        }
    }

    public PlayerActor(ActorContext<Command> context, BufferedImage image, Integer rows, Integer cols, Boolean isGuiOn) {
        super(context);
        this.isGuiOn = isGuiOn;
        this.image = image;
        this.rows = rows;
        this.cols = cols;
        //Getting puzzle data from PuzzleLWWMap (Distributed Data)
        Log.log("getContext().getSelf(): "+getContext().getSelf());
        ActorRef<Receptionist.Listing> subscribeResponseAdapter =
                context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);
        context.getSystem().receptionist().tell(Receptionist.subscribe(PuzzleLWWMap.PUZZLE_SERVICE_KEY, subscribeResponseAdapter));
        //this.puzzleDD.tell(new GetMapMsg(getContext().getSelf()));
    }
    public static Behavior<Command> create(BufferedImage image, Integer rows, Integer cols, Boolean guiOn) {
        return Behaviors.setup(ctx -> new PlayerActor(ctx, image, rows, cols, guiOn));
    }
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetAllMsg.class, this::onGetAllMsg)
                .onMessage(SwapMsg.class, this::onSwapMsg)
                .onMessage(GetViewMsg.class, this::onGetViewMsg)
                .onMessage(ListingResponse.class, this::onListingResponse)
                .build();
    }

    private Behavior<Command> onSwapMsg(SwapMsg msg) {
        Piece piece1 = msg.getPiece();
        Piece piece2 = msg.getSelectedPiece();
        System.out.println("tile1.originalPosition="+piece1.getOriginalPosition()+" - tile1.currentPosition="+piece1.getCurrentPosition());
        System.out.println("tile2.originalPosition="+piece2.getOriginalPosition()+" - tile2.currentPosition="+piece2.getCurrentPosition());
        //Swap the current position
        Piece pTemp = piece1;
        piece1.setCurrentPosition(piece2.getCurrentPosition());
        piece2.setCurrentPosition(pTemp.getCurrentPosition());
        if (puzzleDD.isPresent()) {
            puzzleDD.get().tell(new PuzzleLWWMap.UpdatePiece(piece1.getOriginalPosition().toString(), piece1));
            puzzleDD.get().tell(new PuzzleLWWMap.UpdatePiece(piece2.getOriginalPosition().toString(), piece2));
        }
        puzzleDD.get().tell(new GetMapMsg(getContext().getSelf()));
        return this;
    }
    private Behavior<Command> onGetAllMsg(GetAllMsg msg) {
        System.out.println("onGetAllMsg - start");
        Boolean isPuzzleCompleted = false;
        Map<String, Piece> puzzleMap = msg.getMap();
        List<Piece> pieces = new ArrayList<>(puzzleMap.size());
        //Creating tiles list for the view
        for (Map.Entry<String, Piece> tileEntry : puzzleMap.entrySet()){
            pieces.add(tileEntry.getValue());
            Log.log("Piece - key string: "+tileEntry.getKey()+" Piece: "
                    +tileEntry.getValue().getOriginalPosition()+ ", "
                    +tileEntry.getValue().getCurrentPosition());
        }
        //Checking solution
        if (checkSolution(pieces)) {
            if (isGuiOn) {
                JOptionPane.showMessageDialog(puzzleBoard, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
            }
            isPuzzleCompleted = true;
            Log.log("Puzzle Completed!");
            //TODO: send a message to reset or stop the game!
        }
        //Painting the puzzle on the GUI
        if (isGuiOn) {
            Log.log("Painting puzzle...");
            puzzleBoard.paintPuzzle(image, rows, cols, pieces);
        }
        Log.log("onGetAllMsg - end");
        return this;
    }

    private Behavior<Command> onListingResponse(ListingResponse response) {
        puzzleDD = response.listing.getAllServiceInstances(PuzzleLWWMap.PUZZLE_SERVICE_KEY).stream().findFirst();
        if (puzzleDD.isPresent()) {
            ActorRef<Command> puzzleActor = puzzleDD.get();
            Log.log("DistributedData Actor found: "+puzzleActor);
            puzzleActor.tell(new GetMapMsg(getContext().getSelf()));
        }
        else {
            Log.log("DistributedData PuzzleLWWMap not found.");
            // Non è stato trovato alcun attore con ServiceKey "puzzle"
        }
        return this;
    }



    private Behavior<Command> onGetViewMsg(GetViewMsg command) {
        this.puzzleBoard = command.getPuzzleBoard();
        return this;
    }

    /**
     * Check if any player has won
     * @param pieces
     * @return
     */
    private Boolean checkSolution(List<Piece> pieces) {
        if(pieces!= null && !pieces.isEmpty() && pieces.stream().allMatch(puzzle.actors.Piece::isInRightPlace)) {
            return true;
        }
        else
            return false;
    }
}



