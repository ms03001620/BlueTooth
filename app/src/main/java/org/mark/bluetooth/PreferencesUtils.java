package org.mark.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {
    private static final String SHARE_NAME = "bluetooth_address_default";
    private static final String SHARE_MAC = "device_mac";

    public static String restoreMacAddressFromShare(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SHARE_MAC, "");
    }

    public static void saveMacAddressToShare(Context context, String macString) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(SHARE_MAC, macString).apply();
    }
}
