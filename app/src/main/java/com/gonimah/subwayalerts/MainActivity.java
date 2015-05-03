package com.gonimah.subwayalerts;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gonimah.subwayalerts.models.TravelInformation;
import com.gonimah.subwayalerts.services.ApiClient;
import com.gonimah.subwayalerts.utils.NotificationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final LatLng sDestination = new LatLng(0, 0);
    private static final DateTime sArrivalTime = new DateTime().plusMinutes(45);

    private GoogleApiClient mGoogleApiClient;
    private ApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        mGoogleApiClient = createGoogleApiClient();
        mApiClient = new ApiClient();

        Button pressMeButton = (Button) findViewById(R.id.press_me);
        pressMeButton.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        pressMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleApiClient.connect();
            }
        });

        Button settings = (Button) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startSettings = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(startSettings);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private GoogleApiClient createGoogleApiClient() {
        GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                String toastText = "Null location.";
                if (location != null) {
//                    toastText = String.format("Location: %s, %s", location.getLatitude(), location.getLongitude());
                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                    new ApiClientTask().execute(origin, sDestination, sArrivalTime);
                }

                Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionSuspended(int i) {
                Toast.makeText(MainActivity.this, "Google API Client connection suspended.", Toast.LENGTH_SHORT).show();
            }
        };

        GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Toast.makeText(MainActivity.this, "Google API Client connection failed.", Toast.LENGTH_SHORT).show();
            }
        };

        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(LocationServices.API)
                .build();
    }

    private class ApiClientTask extends AsyncTask<Object, Void, List<TravelInformation>> {
        protected List<TravelInformation> doInBackground(Object... params) {
            LatLng origin = (LatLng) params[0];
            LatLng destination = (LatLng) params[1];
            DateTime arrivalTime = (DateTime) params[2];

            List<TravelInformation> result = null;
            try {
                result = mApiClient.getData(origin, destination, arrivalTime);
            } catch (Exception e) {
                Log.e(TAG, "Unable to fetch api data.", e);
            } finally {
                return result;
            }
        }

        protected void onPostExecute(List<TravelInformation> result) {
            if (result != null && !result.isEmpty()) {
                Notification notification = NotificationUtils.createNotification(MainActivity.this, result.get(0));
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(12345, notification);
            }

//            for (TravelInformation travelInformation : result) {
//                Toast.makeText(MainActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
//            }
        }
    }
}
