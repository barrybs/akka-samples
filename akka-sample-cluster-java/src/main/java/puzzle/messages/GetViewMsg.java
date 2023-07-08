package puzzle.messages;

import puzzle.actors.PuzzleBoard;

import java.io.Serializable;

/**
 * Message to get a ViewFrame reference to update the GUI from the ViewActor
 */
public class GetViewMsg implements Command, Serializable {
    PuzzleBoard puzzle;
    public GetViewMsg(PuzzleBoard puzzle) {
        this.puzzle = puzzle;
    }
    public PuzzleBoard getPuzzleBoard() {
        return puzzle;
    }
    public void setPuzzleBoard(PuzzleBoard puzzle) {
        this.puzzle = puzzle;
    }
}
