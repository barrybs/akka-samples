package puzzle.actors;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
		final String imagePath = "src/main/java/puzzle/actors/bletchley-park-mansion.jpg";

		if (args.length == 0) {
			startupWithRole("distributeddata",25251);
			//startup(25252);
			//startup(0);
			//} else
			//	Arrays.stream(args).map(Integer::parseInt).forEach(App::startup);
		}

		//final PuzzleBoard puzzle = new PuzzleBoard(n, m, imagePath);
        //puzzle.setVisible(true);
	}

	private static Behavior<Void> rootBehavior(Integer rows, Integer cols) {
		return Behaviors.setup(context -> {
			// Create an actor that handles cluster domain events
			context.spawn(MainActor.create(rows, cols), "MainActor");

			return Behaviors.empty();
		});
	}
	private static ActorSystem<Void> startup(int port) {
		// Override the configuration of the port
		// Override the configuration of the port
		Map<String, Object> overrides = new HashMap<>();
		overrides.put("akka.remote.artery.canonical.port", port);

		Config config = ConfigFactory.parseMap(overrides)
				.withFallback(ConfigFactory.load());

		// Create an Akka system
		return ActorSystem.create(rootBehavior(3,4), "ClusterSystem", config);
	}

	private static ActorSystem<Void> startupWithRole(String role, int port) {
		// Override the configuration of the port
		// Override the configuration of the port
		Map<String, Object> overrides = new HashMap<>();
		overrides.put("akka.remote.artery.canonical.port", port);
		overrides.put("akka.cluster.roles", Collections.singletonList(role));

		System.out.println("Application.startupWithRole() - Role:"+ role);

		Config config = ConfigFactory.parseMap(overrides)
				.withFallback(ConfigFactory.load());

		// Create an Akka system
		return ActorSystem.create(rootBehavior(3,4), "ClusterSystem", config);
	}

}
