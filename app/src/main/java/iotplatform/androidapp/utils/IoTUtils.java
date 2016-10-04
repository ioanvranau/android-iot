package iotplatform.androidapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by ioan.vranau on 10/4/2016.
 */

public class IoTUtils {
    public static void showInfoMessage(String messsage, Context context) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, messsage, duration);
        toast.show();
    }

    public static void writePrefference(String text, String key, Activity activity, Context applicationContext) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, String.valueOf(text));
        editor.commit();
        IoTUtils.showInfoMessage("Device ID saved successfully!", applicationContext);
    }

    public static String readPrefference(String key, String defaultValue, Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(key, defaultValue);
    }
}
