package puzzle.messages;

import puzzle.actors.Piece;
import puzzle.actors.Tile;

import java.io.Serializable;

public class SwapMsg implements Command, Serializable {
    private Piece selectedPiece;
    private Piece piece;

    public SwapMsg(Piece selectedPiece, Piece piece) {
        this.selectedPiece = selectedPiece;
        this.piece = piece;
    }

    public SwapMsg(Tile selectedTile, Tile tile) {
        this.selectedPiece = new Piece(selectedTile.getOriginalPosition(), selectedTile.getCurrentPosition());
        this.piece = new Piece(tile.getOriginalPosition(), tile.getCurrentPosition());
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public void setSelectedPiece(Piece selectedPiece) {
        this.selectedPiece = selectedPiece;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece Piece) {
        this.piece = Piece;
    }
}
