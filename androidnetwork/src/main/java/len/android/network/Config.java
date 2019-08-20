package len.android.network;

public class Config {
    private static boolean DEBUG = false;

    public static void isDebug(boolean debug) {
        DEBUG = debug;
    }

    public static boolean isDebug() {
        return DEBUG;
    }
}