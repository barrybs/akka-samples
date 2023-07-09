package puzzle.actors;

import akka.actor.typed.ActorRef;
import puzzle.messages.Command;
import puzzle.messages.SwapMsg;
import puzzle.utils.Log;

public class SelectionManager {
	private boolean selectionActive = false;
	private Tile selectedTile;
	private ActorRef<Command> playerActor;

	public SelectionManager (ActorRef<Command> playerActor){
		this.selectionActive = false;
		this.playerActor = playerActor;
	}
	public void selectTile(final Tile tile, final Listener listener) {
		if(selectionActive) {
			selectionActive = false;
			//swap(selectedTile, tile);
			Log.log("");
			playerActor.tell((new SwapMsg(selectedTile, tile)));
			listener.onSwapPerformed();
		} else {
			selectionActive = true;
			selectedTile = tile;
		}
	}

	private void swap(final Tile t1, final Tile t2) {
		int pos = t1.getCurrentPosition();
		t1.setCurrentPosition(t2.getCurrentPosition());
		t2.setCurrentPosition(pos);
	}

	@FunctionalInterface
	interface Listener{
		void onSwapPerformed();
	}
}


