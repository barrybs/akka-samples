package puzzle.centralized;

/**
 * 
 * Simple Puzzle Game - Centralized version.
 * 
 * By A. Croatti 
 * 
 * @author acroatti
 *
 */
public class Application {

	public static void main(final String[] args) {
		final int n = 3;
		final int m = 5;
		
		//final String imagePath = "src/main/java/pcd/ass03/puzzle/bletchley-park-mansion.jpg";
		final String imagePath = "src/pcd/assignment3/puzzle/centralized/bletchley-park-mansion.jpg";
		
		final PuzzleBoard puzzle = new PuzzleBoard(n, m, imagePath);
        puzzle.setVisible(true);
	}
}
