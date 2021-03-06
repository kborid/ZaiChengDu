package com.z012.chengdu.sc.helper;

public class ForbidFastClickHelper {

    private static final long MAX_DURATION = 500;

    private static long mStart = 0;

    public static boolean isForbidFastClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mStart < MAX_DURATION) {
            return true;
        }
        mStart = currentTime;
        return false;
    }
}
