package com.guomato.snapshot.util;

import android.support.annotation.Nullable;
import android.util.Log;

public class LogHelper {

    private static final String TAG = "TRAINING";

    private static final String LOG_FORMAT = "%s.%s(), Line:%d (%s) %s";

    private static boolean mIsDebugMode = true;

    public static void trace(String extraInfo) {
        if (mIsDebugMode) {
            StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
            String logInfo = String.format(LOG_FORMAT,
                    traceElement.getClassName(),
                    traceElement.getMethodName(),
                    traceElement.getLineNumber(),
                    traceElement.getFileName(),
                    extraInfo == null ? "" : extraInfo);

            Log.d(TAG, logInfo);
        }
    }

}
