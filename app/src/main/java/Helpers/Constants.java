package helpers;

import com.holemcross.deltatrack.data.CtaRoutes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by amortega on 8/25/2016.
 */
public class Constants {
    public static final String CTA_API_BASE_URI = "http://lapi.transitchicago.com/api";
    public static final String CHICAGO_PORTAL_BASE_URI = "https://data.cityofchicago.org";
    public static final String APP_NAME = "Delta-Track";


    public static final String STATION_FRAGMENT_NAME = "StationFragment";
    public static final String SETTINGS_FRAGMENT_NAME = "SettingsFragment";


    public static final String SYSTEM_SETTINGS_NAME = "com.holemcross.deltatrack.system-settings";
    public static final String STATION_FRAGMENT_DATA = "com.holemcross.deltatrack.station-fragment-data";

    public static final Map<String, CtaRoutes> CTA_ROUTES_MAP;
    static{
        Map<String, CtaRoutes> map = new HashMap<String, CtaRoutes>();
        map.put("Red", CtaRoutes.Red);
        map.put("Blue", CtaRoutes.Blue);
        map.put("Brn", CtaRoutes.Brown);
        map.put("G", CtaRoutes.Green);
        map.put("Org", CtaRoutes.Orange);
        map.put("P", CtaRoutes.Purple);
        map.put("Pink", CtaRoutes.Pink);
        map.put("Y", CtaRoutes.Yellow);
        CTA_ROUTES_MAP = Collections.unmodifiableMap(map);
    }

    public static class StationFragment {
        public static final String STATE_MAPID = "com.holemcross.deltatrack.station-fragment.state-mapid";
        public static final String STATE_LAST_REFRESH = "com.holemcross.deltatrack.station-fragment.state-last-refresh";
        public static final String STATE_ARRIVALS = "com.holemcross.deltatrack.station-fragment.state-arrivals";
    }

    public static class Keys {
        public static final String CtaApiKeyName = "CTA_API_KEY";
    }
}
