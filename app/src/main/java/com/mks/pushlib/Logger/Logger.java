package com.mks.pushlib.Logger;

import android.util.Log;

import com.mks.pushlib.C;

public class Logger implements ILogger{
    static String TAG = "PushLib";
    static public void log(String msg) {
        if (C.NEED_LOG)
        {
            Log.d(TAG,"v " + C.CODE_VERSION + ": " + msg);
        }
    }
}
