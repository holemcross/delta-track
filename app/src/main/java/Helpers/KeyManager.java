package helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.holemcross.deltatrack.R;

/**
 * Created by amortega on 8/29/2016.
 */
public class KeyManager {
    private static String LOG_TAG = Constants.APP_NAME + " | " + KeyManager.class.getSimpleName();
    public static String getCtaApiKey(Context context){
        // Get Saved CTA Key
        if(context != null){
            SharedPreferences sharedPref = context.getSharedPreferences(Constants.SYSTEM_SETTINGS_NAME, Context.MODE_PRIVATE);
            String savedCtaApiKey = sharedPref.getString(Constants.SystemSettings.STATE_CTA_API_KEY, null);
            if(!TextUtils.isEmpty(savedCtaApiKey)){
                return savedCtaApiKey;
            }

            // Get Default API Key
            String defaultCtaApiKey = context.getResources().getString(R.string.DEFAULT_CTA_API_KEY);
            if(!TextUtils.isEmpty(defaultCtaApiKey)){
                return defaultCtaApiKey;
            }
        }

        Log.d(LOG_TAG, "Could not find CTA API Key");
        return "";
    }

    public static void saveCtaApiKey(Context context, String newKey){
        if(context != null){
            SharedPreferences sharedPref = context.getSharedPreferences(Constants.SYSTEM_SETTINGS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Constants.SystemSettings.STATE_CTA_API_KEY, newKey);
            editor.commit();
        }
    }
}
