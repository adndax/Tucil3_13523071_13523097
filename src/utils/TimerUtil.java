package utils;

public class TimerUtil {
    private long startTime;
    private long endTime;
    
    // Start timer
    public void start() {
        startTime = System.nanoTime();
    }
    
    // Stop timer
    public void stop() {
        endTime = System.nanoTime();
    }
    
    // Get elapsed time in milliseconds
    public double getElapsedTimeMs() {
        return (endTime - startTime) / 1_000_000.0;
    }
    
    // Reset timer
    public void reset() {
        startTime = 0;
        endTime = 0;
    }
    
    // Format elapsed time with appropriate units
    public String getFormattedElapsedTime() {
        double elapsedMs = getElapsedTimeMs();
        
        if (elapsedMs < 1) {
            return String.format("%.2f µs", elapsedMs * 1000);
        } else if (elapsedMs < 1000) {
            return String.format("%.2f ms", elapsedMs);
        } else {
            return String.format("%.2f s", elapsedMs / 1000);
        }
    }
}