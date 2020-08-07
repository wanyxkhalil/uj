package com.github.wanyxkhalil.uj.id;

/**
 * 雪花算法实现，生成唯一ID
 */
public class Snowflake {

    /**
     * 单例
     */
    private volatile static Snowflake instance;

    /**
     * 属性：集群，机器，序列号，上一次时间戳，基准时间（毫秒）
     */
    private long clusterId;
    private long machineId;
    private long sequence = 0L;
    private long lastMillisecond = -1L;
    private long startTimestamp;

    /**
     * 最大序列数
     */
    private long maxSequence;

    /**
     * 每一部分向左的位移
     */
    private long machineLeft;
    private long clusterLeft;
    private long timestampLeft;

    private Snowflake() {
    }

    /**
     * 读取系统变量，如果无效，使用默认值
     */
    public static Snowflake getInstance(final long clusterBit,
                                        final long machineBit,
                                        final long clusterId,
                                        final long machineId,
                                        final long startTimestamp) {
        if (instance == null) {
            synchronized (Snowflake.class) {
                if (instance == null) {
                    instance = new Snowflake();
                }
            }
        }

        // 校验
        verify(clusterBit, machineBit, clusterId, machineId, startTimestamp);

        // 序列号所占位数
        long sequenceBit = 22 - clusterBit - machineBit;

        // 设置属性
        instance.clusterId = clusterId;
        instance.machineId = machineId;
        instance.startTimestamp = startTimestamp;

        instance.maxSequence = ~(-1L << sequenceBit);

        instance.machineLeft = sequenceBit;
        instance.clusterLeft = sequenceBit + machineBit;
        instance.timestampLeft = instance.clusterLeft + clusterBit;
        return instance;
    }

    /**
     * 校验参数
     */
    private static void verify(long clusterBit, long machineBit, long clusterId, long machineId, long startTimestamp) {
        long maxClusterNum = ~(-1L << clusterBit);
        long maxMachineNum = ~(-1L << machineBit);
        if (clusterId > maxClusterNum || clusterId < 0) {
            throw new IllegalArgumentException("clusterId can't be greater than MAX_CLUSTER_NUM or less than 0");
        }
        if (machineId > maxMachineNum || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }

        if (startTimestamp < 0) {
            throw new IllegalArgumentException("start timestamp can't be less than 0");
        }
    }

    /**
     * 生成ID
     */
    public synchronized long next() {
        long currentMillisecond = currentMillisecond();

        // 时间回拨问题
        if (currentMillisecond < lastMillisecond) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        // 相同毫秒内
        if (currentMillisecond == lastMillisecond) {
            // 序列号自增
            sequence = (sequence + 1) & maxSequence;
            // 同一毫秒的序列数已经达到最大，自旋等待到下一毫秒
            if (sequence == 0L) {
                currentMillisecond = nextMillisecond();
            }
        }
        // 不同毫秒内，序列号置为0
        else {
            sequence = 0L;
        }

        lastMillisecond = currentMillisecond;

        return (currentMillisecond - startTimestamp) << timestampLeft
                | clusterId << clusterLeft
                | machineId << machineLeft
                | sequence;
    }

    private long nextMillisecond() {
        long mill = currentMillisecond();
        while (mill <= lastMillisecond) {
            mill = currentMillisecond();
        }
        return mill;
    }

    private long currentMillisecond() {
        return System.currentTimeMillis();
    }
}
