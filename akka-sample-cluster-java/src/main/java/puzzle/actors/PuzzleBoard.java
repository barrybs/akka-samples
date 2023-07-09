package puzzle.actors;

import akka.actor.typed.ActorRef;
import puzzle.messages.Command;
import puzzle.messages.GetViewMsg;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class PuzzleBoard extends JFrame {
	final int rows, columns;
    final JPanel board;
    final ActorRef<Command> playerActor;
	
	private SelectionManager selectionManager;

	
    public PuzzleBoard(final int rows, final int columns, ActorRef<Command> playerActor) {
    	this.rows = rows;
		this.columns = columns;
        this.playerActor = playerActor;

        selectionManager = new SelectionManager(playerActor);
    	setTitle("Puzzle");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.board = new JPanel();
        this.board.setBorder(BorderFactory.createLineBorder(Color.gray));
        this.board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
        //createTiles(imagePath, puzzleMap);
        //paintPuzzle(board);
    }

    public void paintPuzzle(BufferedImage image, Integer rows, Integer cols, Map<String, Piece> puzzleMap) {
    	board.removeAll();
        List<Tile> tiles = new ArrayList<>();

        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);
        Integer position = 0;
        for (int i = 0; i < rows; i++) {
           for (int j = 0; j < cols; j++) {
            final Image imagePortion = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(),
                new CropImageFilter(j * imageWidth / cols,
                                i * imageHeight / rows,
                                (imageWidth / cols),
                                imageHeight / rows)));
                //Log("paintPuzzle() - position: "+position+", i: "+i+", j: "+j+" "+)
                Tile tile = new Tile(new ImageIcon(imagePortion), position, puzzleMap.get(position.toString()).getCurrentPosition());
                tiles.add(tile);
                position++;
            }
        }
    	Collections.sort(tiles);
    	tiles.forEach(tile -> {
    		final TileButton btn = new TileButton(tile);
            board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> {
            	selectionManager.selectTile(tile, () -> {
            		paintPuzzle(image, rows, cols, puzzleMap);
                	//checkSolution();
            	});
            });
    	});
    	
    	pack();
        //setLocationRelativeTo(null);
    }

    public void sendViewToPlayer(){
        playerActor.tell(new GetViewMsg(this));
    }
    public void display() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
        }); //GUI started...but might not actually be started (invokeLater())
        //Sending ViewFrame reference to the ViewActor for next updates

    }

}
