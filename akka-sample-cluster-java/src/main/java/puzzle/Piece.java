package puzzle;

import akka.cluster.ddata.ReplicatedData;

import java.io.Serializable;

public class Piece implements Serializable {
    private final Integer originalPosition;
    private Integer currentPosition;

    public Piece(Integer originalPosition, Integer currentPosition) {
        this.originalPosition = originalPosition;
        this.currentPosition = currentPosition;
    }

    public Integer getOriginalPosition() {
        return originalPosition;
    }

    public Integer getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Integer currentPosition) {
        this.currentPosition = currentPosition;
    }
/*
    @Override
    public Piece merge(ReplicatedData other) {
        if (other instanceof Piece) {
            Piece otherPiece = (Piece) other;
            // In caso di conflitto, prende la posizione corrente del pezzo che ha la posizione originale più bassa.
            // Se entrambi i pezzi hanno la stessa posizione originale, prende la posizione corrente più bassa.
            // Questa è solo una possibile implementazione del metodo merge(). La tua logica potrebbe differire.
            if (this.originalPosition < otherPiece.originalPosition ||
                    (this.originalPosition == otherPiece.originalPosition && this.currentPosition < otherPiece.currentPosition)) {
                return this;
            }
            else {
                return otherPiece;
            }
        }
        else {
            throw new IllegalArgumentException("Cannot merge with non-Piece data");
        }
    }
    // Override dei metodi equals e hashCode come prima

 */
}
