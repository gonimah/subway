package com.gonimah.subwayalerts;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gonimah.subwayalerts.models.TravelInformation;
import com.gonimah.subwayalerts.services.ApiClient;
import com.gonimah.subwayalerts.utils.NotificationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ANIMATION_DURATION_MS = 100;
    private static final LatLng sDestination = new LatLng(0, 0);
    private static final DateTime sArrivalTime = new DateTime().plusMinutes(45);

    private ViewGroup mRoot;
    private ImageView mEditButton;
    private TextView mTimeLabel;
    private TextView mDestinationLabel;
    private Spinner mDesinationSpinner;
    private TextView mAddDestination;

    private GoogleApiClient mGoogleApiClient;
    private ApiClient mApiClient;
    private boolean mIsEdit;
    private int mColorSubway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        mRoot = (ViewGroup) findViewById(R.id.root);


        mGoogleApiClient = createGoogleApiClient();
        mApiClient = new ApiClient();
        mIsEdit = false;
        mColorSubway = getResources().getColor(R.color.subway);

        Typeface fontVerano = Typeface.createFromAsset(getAssets(), "fonts/Verano/Verano.otf");

        mEditButton = (ImageView) findViewById(R.id.edit);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsEdit = !mIsEdit;

                Integer colorFrom;
                Integer colorTo;
                if (mIsEdit) {
                    colorFrom = new Integer(mColorSubway);
                    colorTo = Color.WHITE;
                } else {
                    colorFrom = Color.WHITE;
                    colorTo = new Integer(mColorSubway);
                }

                ValueAnimator backgroundAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                backgroundAnimation.setDuration(ANIMATION_DURATION_MS);
                backgroundAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        mRoot.setBackgroundColor((Integer) animator.getAnimatedValue());
                    }
                });

                ValueAnimator contentAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
                contentAnimation.setDuration(ANIMATION_DURATION_MS);
                contentAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mEditButton.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mEditButton.setEnabled(true);

                        if (mIsEdit) {
                            mDestinationLabel.setVisibility(View.GONE);
                            mDesinationSpinner.setVisibility(View.VISIBLE);
                            mAddDestination.setVisibility(View.VISIBLE);
                        } else {
                            mDesinationSpinner.setVisibility(View.GONE);
                            mAddDestination.setVisibility(View.GONE);
                            mDestinationLabel.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                });
                contentAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        int color = (Integer) animator.getAnimatedValue();
                        mEditButton.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                        mTimeLabel.setTextColor(color);
                        mDestinationLabel.setTextColor(color);
                    }
                });

                backgroundAnimation.start();
                contentAnimation.start();
            }
        });

        mTimeLabel = (TextView) findViewById(R.id.time);
        mTimeLabel.setTypeface(fontVerano);
        mTimeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsEdit) {
                    TimePickerDialog timePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            NumberFormat formatter = new DecimalFormat("00");
                            mTimeLabel.setText(selectedHour + ":" + formatter.format(selectedMinute));
                        }
                    }, 0, 0, true);
                    timePicker.setTitle("Select Time");
                    timePicker.show();
                } else {
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (location != null) {
                        LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                        new ApiClientTask().execute(origin, sDestination, sArrivalTime);
                    }
                }
            }
        });

        mDestinationLabel = (TextView) findViewById(R.id.destination);
        mDestinationLabel.setTypeface(fontVerano);

        mDesinationSpinner = (Spinner) findViewById(R.id.destination_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.destinations, R.layout.destination_spinner_view);
        adapter.setDropDownViewResource(R.layout.destination_spinner_view);
        mDesinationSpinner.setAdapter(adapter);
        mDesinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String destination = (String) parent.getItemAtPosition(position);
                mDestinationLabel.setText(destination);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        mAddDestination = (TextView) findViewById(R.id.add_destination);
        mAddDestination.setTypeface(fontVerano);

        mGoogleApiClient.connect();
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
//                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//                if (location != null) {
//                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
//                    new ApiClientTask().execute(origin, sDestination, sArrivalTime);
//                }
            }

            @Override
            public void onConnectionSuspended(int i) {
//                Toast.makeText(MainActivity.this, "Google API Client connection suspended.", Toast.LENGTH_SHORT).show();
            }
        };

        GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
//                Toast.makeText(MainActivity.this, "Google API Client connection failed.", Toast.LENGTH_SHORT).show();
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
