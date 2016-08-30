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
    public static String GetCtaApiKey(Context context){
        // Get Saved CTA Key
        if(context != null){
            SharedPreferences sharedPref = context.getSharedPreferences(Constants.SYSTEM_SETTINGS_NAME, Context.MODE_PRIVATE);
            String savedCtaApiKey = sharedPref.getString(Constants.Keys.CtaApiKeyName, null);
            if(TextUtils.isEmpty(savedCtaApiKey)){
                return savedCtaApiKey;
            }
        }

        // Get Default API Key
        String defaultCtaApiKey = Resources.getSystem().getString(R.string.DEFAULT_CTA_API_KEY);
        if(TextUtils.isEmpty(defaultCtaApiKey)){
            return defaultCtaApiKey;
        }

        Log.d(LOG_TAG, "Could not find CTA API Key");
        return "";
    }
}
