package com.tsw.blockchain.common;

import java.util.concurrent.TimeUnit;

public class Constants {
    private Constants() {
    }
    public static final long EFFECTIVE_TIME_LOCAL_REPUTATION = TimeUnit.DAYS.toMillis(30);
    public static final double DEFAULT_BIG_MAC = 1;
    public static long T0 = 0L;
}
