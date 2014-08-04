package com.kii.wearable.demo;

/**
 * Created by tian on 14-6-24.
 */
public class Utils {
    public static boolean newerThan(long compareTime, int threshold) {
        return compareTime > (System.currentTimeMillis() - threshold);
    }
}
