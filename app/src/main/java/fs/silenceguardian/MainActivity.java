package fs.silenceguardian;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "MainActivity";

    private GoogleApiClient     mGoogleApiClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent       mGeofencePendingIntent;
    private boolean             mGeofencesAdded;
    private StateHolder         stateHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stateHolder = StateHolder.getStateHolder(this);
        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;

        mGeofencesAdded = stateHolder.isGeofenceAdded();
        populateGeofenceList();
        buildGoogleApiClient();

        SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id.cb_enable_app);
        switchCompat.setChecked(stateHolder.isAutoSilentEnabled());
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                stateHolder.setAutoSilentEnabled(checked);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        mGeofencePendingIntent = null;
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);

        return builder.build();
    }

    public void AddGeofencing() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
            "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (!mGeofencesAdded) {
            AddGeofencing();
        }
    }

    @Override public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " +
            result.getErrorCode() + " getErrorMessage = " + result.getErrorMessage());
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void populateGeofenceList() {
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

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            mGeofencesAdded = !mGeofencesAdded;
            stateHolder.setGeofenceAdded(mGeofencesAdded);

            Toast.makeText(
                this,
                getString(mGeofencesAdded ? R.string.geofences_added :
                    R.string.geofences_removed),
                Toast.LENGTH_SHORT
            ).show();
        } else {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }
}
