package org.xiaomi.utils;

public class SnowflakeIdGenerator {

    // 每部分占用位数
    private final long nodeIdBits = 10L;
    private final long sequenceBits = 12L;

    // 最大值
    private final long maxSequence = ~(-1L << sequenceBits);

    private long nodeId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator(long nodeId) {
        if (nodeId < 0 || nodeId > ~(-1L << nodeIdBits)) {
            throw new IllegalArgumentException("Node ID must be between 0 and " + (~(-1L << nodeIdBits)));
        }
        this.nodeId = nodeId << sequenceBits;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return (timestamp << (nodeIdBits + sequenceBits))
                | (nodeId)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}