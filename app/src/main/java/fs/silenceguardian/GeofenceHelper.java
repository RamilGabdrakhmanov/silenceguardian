package fs.silenceguardian;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by ramilgabdrakhmanov on 11/30/16.
 */

public class GeofenceHelper {

    private static final String TAG = "GeofenceHelper";

    private PendingIntent          mGeofencePendingIntent;
    private ArrayList<Geofence>    mGeofenceList;
    private Context                context;
    private ResultCallback<Status> listener;

    public GeofenceHelper(Context context, ResultCallback<Status> listener) {
        mGeofencePendingIntent = null;
        this.listener = listener;
        this.context = context;

        mGeofenceList = new ArrayList<>();
        mGeofenceList.add(new Geofence.Builder()
            .setRequestId("Flatstack House")
            .setCircularRegion(
                55.793764, 49.125353, 10
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_EXIT)
            .build());
    }

    public void addGeofencing(GoogleApiClient mGoogleApiClient) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(context, context.getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
            ).setResultCallback(listener);
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);

        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
            "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }
}
