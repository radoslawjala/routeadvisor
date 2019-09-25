package com.example.radek.mapsproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class NewRouteMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button start_route_button, stop_route_button, start_location;
    TextView time_field, distance_field;

    private ArrayList<LatLng> points;
    private ArrayList<Double> route;
    private ArrayList<Integer> time_table;
    private ArrayList<Double> coordinates_table;
    ArrayList<Double> threeCoordinates;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;

    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1500;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    private static final String TAG = NewRouteMapsActivity.class.getSimpleName();
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Location mOldLocation;
    private Handler handler = new Handler();
    long startTime = 0L, timeInMilliseconds = 0L, timeSwapBuff = 0L, updateTime = 0L;
    int seconds;
    private Boolean mRequestingLocationUpdates;
    LocationPrerequisites lp;
    DatabaseHelper databaseHelper;

    double distance, wholeDistance, velocity;
    String dis;
    //String vel;
    boolean first = true;
    boolean routemodule;
    String routeName;
    LatLng place;
    double lat;
    double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_route_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        databaseHelper = new DatabaseHelper(getApplicationContext());
        Intent intent = getIntent();
        routeName = intent.getStringExtra("routeName");
        lp = new LocationPrerequisites();
        start_route_button = findViewById(R.id.start_route_button);
        stop_route_button = findViewById(R.id.stop_route_button);
        start_location = findViewById(R.id.startLocationUpdates);
        time_field = findViewById(R.id.time_field);
        distance_field = findViewById(R.id.distance_field);
        distance = 0.0;
        wholeDistance = 0.0;
        //velocity = 0.0;
       // vel = " ";
        dis = " ";
        routemodule = false;

        start_route_button.setEnabled(false);
        stop_route_button.setEnabled(false);
        points = new ArrayList<>();
        route = new ArrayList<>();
        time_table = new ArrayList<>();
        coordinates_table = new ArrayList<>();
        threeCoordinates = new ArrayList<>();

        init();
        restoreValuesFromBundle(savedInstanceState);
       // start();

    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                if(first) {
                    mOldLocation = mCurrentLocation;
                    first = false;
                }

                updateLocationUI();
                updateDistanceAndVelocity();

                mOldLocation = mCurrentLocation;
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // mLocationRequest.setSmallestDisplacement(5);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @SuppressLint("DefaultLocale")
    private void updateDistanceAndVelocity() {

        distance = computeDistance(mOldLocation, mCurrentLocation);
        wholeDistance = wholeDistance + distance;
        //dis = String.format("%.2f", wholeDistance);
        double d = Math.round(wholeDistance);
        dis = Double.toString(d);
        lat = mCurrentLocation.getLatitude();
        lon = mCurrentLocation.getLongitude();

/*
        velocity = computeVelocity(mOldLocation, mCurrentLocation, distance);
        vel = String.format("%.2f", velocity);
        */
    }
/*
    private double computeVelocity(Location mOldLocation, Location mCurrentLocation, double distance) {


        double speed = distance / (mCurrentLocation.getTime() - mOldLocation.getTime());
        if (mCurrentLocation.hasSpeed()) {
            speed = mCurrentLocation.getSpeed();
        }
        speed = speed *3.6;
        return speed;

    }
*/
    private double computeDistance(Location mOldLocation, Location mCurrentLocation) {

        LatLng from = new LatLng(mOldLocation.getLatitude(), mOldLocation.getLongitude());
        LatLng to = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        return SphericalUtil.computeDistanceBetween(from, to);

    }

    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if(savedInstanceState.containsKey("distance")) {
                distance = savedInstanceState.getDouble("distance");
            }

            if(savedInstanceState.containsKey("velocity")) {
                velocity = savedInstanceState.getDouble("velocity");
            }
        }

        updateLocationUI();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateLocationUI() {


        if (mCurrentLocation != null) {

            mMap.clear();

            place = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            if(routemodule) {
                points.add(place);
                //redrawLine();
            }
            mMap.addMarker(new MarkerOptions().position(place));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        }

    }
/*
    private void redrawLine() {
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        Polyline line = mMap.addPolyline(options);
    }
*/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putDouble("distance", distance);
        //outState.putDouble("velocity", velocity);

    }

    public void startLocationClick(View view) {
        start();
    }

    public void start() {

        mRequestingLocationUpdates = lp.startClick(this);
        if(mRequestingLocationUpdates) {
            lp.startLocationUpdates(mSettingsClient, mLocationSettingsRequest,
                    mFusedLocationClient, mLocationRequest,
                    mLocationCallback, this);
        }
        start_route_button.setEnabled(true);
        updateLocationUI();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRequestingLocationUpdates && checkPermissions()) {
            lp.startLocationUpdates(mSettingsClient, mLocationSettingsRequest,
                    mFusedLocationClient, mLocationRequest,
                    mLocationCallback, this);
        }

        updateLocationUI();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    int how_many_times = 0;
    @SuppressLint("SetTextI18n")
    public void startRouteClick(View view) {

        how_many_times = 0;
        points.clear();
        distance = wholeDistance = velocity = 0.0;
        distance_field.setText("Dystans: 0,00 m");
       // velocity_field.setText("Velocity: 0,00 km /h");
        start_route_button.setEnabled(false);
        stop_route_button.setEnabled(true);
        startTime = SystemClock.uptimeMillis();
        routemodule = true;
        runnable.run();
        runnable2.run();
    }

    private Runnable runnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            distance_field.setText("Dystans: " +  dis + " m");
            distance_field.setAlpha(0);
            distance_field.animate().alpha(1).setDuration(300);

/*
            velocity_field.setText("Velocity: " + vel + " km/h");
            velocity_field.setAlpha(0);
            velocity_field.animate().alpha(1).setDuration(300);
*/
            how_many_times++;
            route.add(wholeDistance);
            time_table.add(seconds);
            coordinates_table.add(lat);
            coordinates_table.add(lon);

            handler.postDelayed(this, 10000);
        }
    };

    private Runnable runnable2 = new Runnable() {
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updateTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updateTime / 1000);
            int mins = secs / 60;
            seconds = secs;
            secs %= 60;
            time_field.setText("" + mins + " min " + String.format("%2d", secs) + " s");
            handler.postDelayed(runnable2, 0);
        }
    };
/*
    private void getThreeCoordinates() {

        int half = coordinates_table.size() / 2;
        threeCoordinates.add(coordinates_table.get(0));
        threeCoordinates.add(coordinates_table.get(1));
        if (half % 2 == 1) {
            threeCoordinates.add(coordinates_table.get(half-1));
            threeCoordinates.add(coordinates_table.get(half));
        }
        else {
            threeCoordinates.add(coordinates_table.get(half));
            threeCoordinates.add(coordinates_table.get(half+1));
        }
        threeCoordinates.add(coordinates_table.get(coordinates_table.size()-2));
        threeCoordinates.add(coordinates_table.get(coordinates_table.size()-1));

    }
*/
    @SuppressLint("SetTextI18n")
    public void stopRouteClick(View view) {


        route.add(wholeDistance);
        time_table.add(seconds);
        handler.removeCallbacks(runnable);
        handler.removeCallbacks(runnable2);
        timeSwapBuff = 0;
        distance = wholeDistance = velocity = 0.0;
        distance_field.setText("Dystans: 0,00 m");
        //velocity_field.setText("Velocity: 0,00 km/h");
        start_route_button.setEnabled(true);
        stop_route_button.setEnabled(false);
        routemodule = false;
        mMap.clear();
        points.clear();
        insertRouteIntoDB(route, time_table, routeName, coordinates_table);
    }

    private void insertRouteIntoDB(ArrayList<Double> route, ArrayList<Integer> time_table, String routeName, ArrayList<Double> coordinates) {
        Gson gson = new Gson();
        String inputArrayString = gson.toJson(route);
        String inputArrayStringTime = gson.toJson(time_table);
        String inputArrayStringCoordinates = gson.toJson(coordinates);
        databaseHelper.updateJson(routeName, inputArrayString);
        databaseHelper.updateTime(routeName, inputArrayStringTime);
        databaseHelper.updateCoordinates(routeName, inputArrayStringCoordinates);
        Intent intent = new Intent(NewRouteMapsActivity.this, MainActivity.class);
        NewRouteMapsActivity.this.startActivity(intent);
    }


}

