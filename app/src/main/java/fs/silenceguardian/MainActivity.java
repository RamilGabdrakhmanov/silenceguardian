package fs.silenceguardian;

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
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "MainActivity";

    private GoogleApiClient     mGoogleApiClient;

    private boolean             mGeofencesAdded;
    private StateHolder         stateHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stateHolder = StateHolder.getStateHolder(this);

        mGeofencesAdded = stateHolder.isGeofenceAdded();
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

    @Override public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (!mGeofencesAdded) {
            new GeofenceHelper(this, this).addGeofencing(mGoogleApiClient);
        }
    }

    @Override public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " +
            result.getErrorCode() + " getErrorMessage = " + result.getErrorMessage());
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
