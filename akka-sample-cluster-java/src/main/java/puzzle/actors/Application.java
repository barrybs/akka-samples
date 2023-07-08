package puzzle.actors;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import puzzle.utils.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Simple Puzzle Game - Distributed version.
 * 
 * By Barry Bassi
 * 
 * @author barry.bassi
 *
 */
public class Application {

	public static void main(final String[] args) {
		//src/main/java/pcd/ass03/puzzle/bletchley-park-mansion.jpg

		//final String imagePath = "src/main/java/puzzle/bletchley-park-mansion.jpg";
		final String imagePath = "bletchley-park-mansion.jpg";

		final BufferedImage image;
		try {
			image = ImageIO.read(new File(imagePath));
		} catch (IOException ex) {
			System.out.println("Could not load image. Exception: "+ex);
			return;
		}
		if (args.length == 0) {
			Log.log(startupWithRole("distributeddata", image, 25251).toString());
			Log.log(startupWithRole("gamerwithgui", image, 25252).toString());

			//startup(25252);
			//startup(0);
			//} else
			//	Arrays.stream(args).map(Integer::parseInt).forEach(App::startup);
		}

		//final PuzzleBoard puzzle = new PuzzleBoard(n, m, imagePath);
        //puzzle.setVisible(true);
	}

	private static Behavior<Void> rootBehavior(Integer rows, Integer cols, BufferedImage image) {
		return Behaviors.setup(context -> {
			// Create an actor that handles cluster domain events
			context.spawn(MainActor.create(rows, cols, image), "MainActor");

			return Behaviors.empty();
		});
	}
	private static ActorSystem<Void> startup(int port, BufferedImage image) {
		// Override the configuration of the port
		// Override the configuration of the port
		Map<String, Object> overrides = new HashMap<>();
		overrides.put("akka.remote.artery.canonical.port", port);

		Config config = ConfigFactory.parseMap(overrides)
				.withFallback(ConfigFactory.load());

		// Create an Akka system
		return ActorSystem.create(rootBehavior(3,4, image), "ClusterSystem", config);
	}

	private static ActorSystem<Void> startupWithRole(String role, BufferedImage image, int port) {
		// Override the configuration of the port
		// Override the configuration of the port
		Map<String, Object> overrides = new HashMap<>();
		overrides.put("akka.remote.artery.canonical.port", port);
		overrides.put("akka.cluster.roles", Collections.singletonList(role));

		System.out.println("Application.startupWithRole() - Role:"+ role);

		Config config = ConfigFactory.parseMap(overrides)
				.withFallback(ConfigFactory.load());

		// Create an Akka system
		return ActorSystem.create(rootBehavior(3,4, image), "ClusterSystem", config);
	}

}
