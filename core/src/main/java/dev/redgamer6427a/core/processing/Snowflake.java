package dev.redgamer6427a.core.processing;

public class Snowflake {
    // Custom epoch (Discord: 2015-01-01)
    private static final long EPOCH = 1420070400000L;

    // Worker and process IDs (0–31)
    private static final int WORKER_ID = 1;
    private static final int PROCESS_ID = 1;

    private static long sequence = 0L;        // 12-bit counter
    private static long lastTimestamp = -1L;  // Last timestamp used

    private static final int WORKER_BITS = 5;
    private static final int PROCESS_BITS = 5;
    private static final int SEQUENCE_BITS = 12;

    private static final int MAX_WORKER_ID = (1 << WORKER_BITS) - 1;
    private static final int MAX_PROCESS_ID = (1 << PROCESS_BITS) - 1;
    private static final int MAX_SEQUENCE = (1 << SEQUENCE_BITS) - 1;

    private static final int PROCESS_SHIFT = SEQUENCE_BITS;
    private static final int WORKER_SHIFT = SEQUENCE_BITS + PROCESS_BITS;
    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + PROCESS_BITS + WORKER_BITS;

    static {
        if (WORKER_ID < 0 || WORKER_ID > MAX_WORKER_ID)
            throw new IllegalArgumentException("workerId out of range");
        if (PROCESS_ID < 0 || PROCESS_ID > MAX_PROCESS_ID)
            throw new IllegalArgumentException("processId out of range");
    }

    public static synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(timestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | ((long) WORKER_ID << WORKER_SHIFT)
                | ((long) PROCESS_ID << PROCESS_SHIFT)
                | sequence;
    }

    private static long waitNextMillis(long currentTimestamp) {
        long ts = currentTimestamp;
        while (ts <= lastTimestamp) {
            ts = System.currentTimeMillis();
        }
        return ts;
    }

    // Optional: extract timestamp from Snowflake
    public static long extractTimestamp(long snowflakeId) {
        return (snowflakeId >> TIMESTAMP_SHIFT) + EPOCH;
    }
}
