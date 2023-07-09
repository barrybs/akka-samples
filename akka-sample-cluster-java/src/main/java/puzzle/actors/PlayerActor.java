package puzzle.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import puzzle.messages.*;
import puzzle.utils.Log;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.*;

public final class PlayerActor extends AbstractBehavior<Command> {
    private final Boolean isGuiOn;
    private PuzzleBoard puzzleBoard;
    private Optional<ActorRef<Command>> puzzleDD;
    private final BufferedImage image;
    private final Integer rows;
    private final Integer cols;
    public static ServiceKey<Command> PLAYER_SERVICE_KEY = ServiceKey.create(Command.class, "playerService");

    public static final class PuzzleLWWMapResponseAdapter implements Command {
        final Receptionist.Listing listing;
        public PuzzleLWWMapResponseAdapter(Receptionist.Listing listing) {
            this.listing = listing;
        }
    }

    public static final class PuzzleUpdatedResponseAdapter implements Command {
        //public static final PuzzleUpdatedResponseAdapter INSTANCE = new PuzzleUpdatedResponseAdapter();
        final Receptionist.Listing listing;

        private PuzzleUpdatedResponseAdapter(Receptionist.Listing listing) {
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
                context.messageAdapter(Receptionist.Listing.class, PuzzleLWWMapResponseAdapter::new);
        context.getSystem().receptionist().tell(Receptionist.subscribe(PuzzleLWWMap.PUZZLE_SERVICE_KEY, subscribeResponseAdapter));
        //this.puzzleDD.tell(new GetMapMsg(getContext().getSelf()));
    }
    public static Behavior<Command> create(BufferedImage image, Integer rows, Integer cols, Boolean guiOn) {
        return Behaviors.setup(ctx -> {
            ActorRef<Command> self = ctx.getSelf();
            ctx.getSystem().receptionist().tell(Receptionist.register(PLAYER_SERVICE_KEY, self.narrow()));
            return new PlayerActor(ctx, image, rows, cols, guiOn);
        });
       /* return Behaviors.withTimers(timers -> {
            //timers.startTimerAtFixedRate(UpdateTimer.INSTANCE, Duration.ofSeconds(1)); // ogni 1 secondo
            return Behaviors.setup(ctx -> new PlayerActor(ctx, image, rows, cols, guiOn));
        });

        */
    }
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetAllMsg.class, this::onGetAllMsg)
                .onMessage(SwapMsg.class, this::onSwapMsg)
                .onMessage(GetViewMsg.class, this::onGetViewMsg)
                .onMessage(PuzzleLWWMapResponseAdapter.class, this::onListingResponse)
                .onMessage(PuzzleUpdatedResponseAdapter.class, this::onPuzzleUpdated)
                .build();
    }
    private Behavior<Command> onPuzzleUpdated(PuzzleUpdatedResponseAdapter response) {
        Set<ActorRef<Command>> playersSet = response.listing.getAllServiceInstances(PLAYER_SERVICE_KEY);
        if (puzzleDD.isPresent()) {
            for (ActorRef<Command> player : playersSet) {
                puzzleDD.get().tell(new GetMapMsg(player));
            }
        }
        return this;
    }
    private Behavior<Command> onSwapMsg(SwapMsg msg) {
        Piece piece1 = msg.getPiece();
        Piece piece2 = msg.getSelectedPiece();
        //Swap the current position of the piece
        Piece pTemp = new Piece (piece1.getOriginalPosition(), piece1.getCurrentPosition());
        piece1.setCurrentPosition(piece2.getCurrentPosition());
        piece2.setCurrentPosition(pTemp.getCurrentPosition());
        if (puzzleDD.isPresent()) {
            puzzleDD.get().tell(new PuzzleLWWMap.UpdatePiece(piece1.getOriginalPosition().toString(), piece1));
            puzzleDD.get().tell(new PuzzleLWWMap.UpdatePiece(piece2.getOriginalPosition().toString(), piece2));
        }
        //Sending a GetMapMsg to all players after the update to get the updated puzzle
        ActorRef<Receptionist.Listing> puzzleUpdateResponseAdapter =
                getContext().messageAdapter(Receptionist.Listing.class, PuzzleUpdatedResponseAdapter::new);
        getContext().getSystem().receptionist().tell(Receptionist.find(PLAYER_SERVICE_KEY, puzzleUpdateResponseAdapter));
        //puzzleDD.get().tell(new GetMapMsg(getContext().getSelf()));
        return this;
    }
    private Behavior<Command> onGetAllMsg(GetAllMsg msg) {
        Log.log("onGetAllMsg - start");
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
        if (isGuiOn) {
            Log.log("Painting puzzle...");
            puzzleBoard.paintPuzzle(image, rows, cols, puzzleMap);
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
        Log.log("onGetAllMsg - end");
        return this;
    }

    private Behavior<Command> onListingResponse(PuzzleLWWMapResponseAdapter response) {
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
        if(pieces != null && !pieces.isEmpty() && pieces.stream().allMatch(puzzle.actors.Piece::isInRightPlace)) {
            Log.log("Check solution: true.");
            return true;
        }
        else {
            Log.log("Check solution: false.");
            return false;
        }
    }
}



