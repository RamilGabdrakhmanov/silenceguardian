package fs.silenceguardian;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ramilgabdrakhmanov on 11/24/16.
 */

public class StateHolder {
    static private StateHolder stateHolder;

    private SharedPreferences mSharedPreferences;

    public static StateHolder getStateHolder(Context context) {
        if (stateHolder == null) {
            stateHolder = new StateHolder(context);
        }

        return stateHolder;
    }

    public StateHolder(Context context) {
        mSharedPreferences = context.getSharedPreferences("flatstack", MODE_PRIVATE);
    }

    public boolean isGeofenceAdded() {
        return getBoolean("GeofencesAdded", false);
    }

    public void setGeofenceAdded(boolean geofencesAdded) {
        setBoolen("GeofencesAdded", geofencesAdded);
    }

    public boolean isAutoSilentEnabled() {
        return getBoolean("autoSilentEnabled", false);
    }

    public void setAutoSilentEnabled(boolean autoSilentEnabled) {
        setBoolen("autoSilentEnabled", autoSilentEnabled);
    }

    private boolean getBoolean(String key, boolean def) {
        return mSharedPreferences.getBoolean(key, def);
    }

    private void setBoolen(String key, boolean val) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, val);
        editor.apply();
    }
}
