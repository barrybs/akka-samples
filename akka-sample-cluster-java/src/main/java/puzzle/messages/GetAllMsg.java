package puzzle.messages;

import puzzle.actors.Piece;

import java.io.Serializable;
import java.util.Map;

public class GetAllMsg implements Command, Serializable {
    private Map<String, Piece> map;

    public GetAllMsg(Map<String, Piece> map) {
        this.map = map;
    }

    public Map<String, Piece> getMap() {
        return map;
    }

    public void setMap(Map<String, Piece> map) {
        this.map = map;
    }
}
