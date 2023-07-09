package puzzle.actors;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.LWWMap;
import akka.cluster.ddata.LWWMapKey;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import puzzle.messages.Command;
import puzzle.utils.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class App {
    private static class RootBehavior {
        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                Cluster cluster = Cluster.get(context.getSystem());
                //String imagePath = context.getSystem().settings().config().getString("puzzle-image-path");
                String imagePath = "bletchley-park-mansion.jpg";
                final BufferedImage image;
                try {
                    image = ImageIO.read(new File(imagePath));
                } catch (IOException ex) {
                    System.out.println("Could not load image. Exception: "+ex);
                    return Behaviors.stopped();
                }
                Integer rows = 4;
                Integer cols = 4;

                if (cluster.selfMember().hasRole("distributeddata")) {
                    final Key<LWWMap<String, Piece>> puzzleKey = LWWMapKey.create("puzzle");
                    final ActorRef<Command> puzzleDD = context.spawn(PuzzleLWWMap.create(puzzleKey), "puzzle");
                    final List<Integer> randomPositions = new ArrayList<>();

                    IntStream.range(0, rows * cols).forEach(item -> {
                        randomPositions.add(item);
                    });
                    Collections.shuffle(randomPositions);

                    //Test Map
                    Map<Integer, Integer> testMap = new HashMap<>();
                    Tile tempPiece = new Tile(null,0,0);

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
                            //Tile tile = new Tile(new ImageIcon(imagePortion), position, randomPositions.get(position));
                            Piece piece = new Piece(position, randomPositions.get(position));
                            //Writing the piece on LWWMap
                            puzzleDD.tell(new PuzzleLWWMap.UpdatePiece(piece.getOriginalPosition().toString(),piece));
                            testMap.put(piece.getOriginalPosition(), piece.getCurrentPosition());
                            position++;
                        }
                    }
                   // puzzleDD.tell(new GetMapMsg(context.getSelf()));
                }
                if (cluster.selfMember().hasRole("gamerwithgui")) {
                    ActorRef<Command> playerActor = context.spawn(PlayerActor.create(image, rows, cols, true), "playerActor");
                    PuzzleBoard puzzle = new PuzzleBoard(rows, cols, playerActor);
                    puzzle.sendViewToPlayer();
                    puzzle.display();
                    Log.log("Tell GetMapMsg to itself");
                }
/*
                if (cluster.selfMember().hasRole("backend")) {
                    int workersPerNode = context.getSystem().settings().config().getInt("transformation.workers-per-node");
                    for (int i = 0; i < workersPerNode; i++) {
                        context.spawn(Worker.create(), "Worker" + i);
                    }
                }
                if (cluster.selfMember().hasRole("frontend")) {
                    context.spawn(Frontend.create(), "Frontend");
                }
*/
                return Behaviors.empty();
            });
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            startup("distributeddata", 25251);
            startup("gamerwithgui", 25252);
            startup("gamerwithgui", 0);
           /* startup("frontend", 0);
            startup("frontend", 0);
            startup("frontend", 0);*/
        } else {
            if (args.length != 2)
                throw new IllegalArgumentException("Usage: role port");
            startup(args[0], Integer.parseInt(args[1]));
        }
    }

    private static void startup(String role, int port) {

        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load());

        ActorSystem<Void> system = ActorSystem.create(RootBehavior.create(), "ClusterSystem", config);
    }
}