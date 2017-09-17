package com.example.a90678.android2droid1707302025.Util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by 90678 on 2017/9/14.
 */

public class SpUtil {

    private static final String KX = "kx";
    private static final String IP = "ip";

    public static SharedPreferences getSp(Context context) {
        return context.getSharedPreferences(KX, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getSpEditor (Context context) {
        return getSp(context).edit();
    }

    public static void setIp (Context context, String ip) {
        getSpEditor(context).putString(IP, ip).apply();
    }

    public static String getIp (Context context) {
        return getSp(context).getString(IP, "");
    }
}
