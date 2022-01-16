package cf.hydos.engine.core;

public class Time {
    private static final long SECOND_IN_NANOSECONDS = 1_000_000_000L;

    public static double GetTime() {
        return (double) System.nanoTime() / (double) SECOND_IN_NANOSECONDS;
    }
}
