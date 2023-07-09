package puzzle.utils;

public class Log {
    static Boolean active = false;
    public static void log (String text){
        if (active) {
            System.out.println("LOG: "+text);
        }
    }
}
