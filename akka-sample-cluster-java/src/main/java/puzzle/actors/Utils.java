package puzzle.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import pcd.assignment3.puzzle.actors.messages.PuzzleMsg;

public class Utils {

    public static ActorRef<PuzzleMsg> startup(String configFile, Integer port){
        Config config = ConfigFactory.parseString("akka.remote.artery.canonical.port="+port)
                                     .withFallback(ConfigFactory.load(configFile));
        return ActorSystem.create(MainActor.create(), "MainActor");




    }
}
