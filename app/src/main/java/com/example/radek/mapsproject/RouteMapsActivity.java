package com.example.radek.mapsproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.SphericalUtil;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


public class RouteMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button start_route_button, stop_route_button, start_location;
    TextView time_left_field, distance_field;

    private ArrayList<LatLng> points;
    private ArrayList<Double> route;
    private ArrayList<Integer> time_table;
    private ArrayList<Double> coordinates_table;

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
    //long startTime = 0L, timeInMilliseconds = 0L, timeSwapBuff = 0L, updateTime = 0L;
    private Boolean mRequestingLocationUpdates;
    LocationPrerequisites lp;
    DatabaseHelper databaseHelper;
    double distance, wholeDistance, velocity;
    String vel, dis;
    boolean first = true;
    boolean routemodule;
    LatLng place;
    String routeName;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_maps);
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
        time_left_field = findViewById(R.id.time_left_field);
        distance_field = findViewById(R.id.distance_field);
        distance = 0.0;
        wholeDistance = 0.0;
        velocity = 0.0;
        vel = " ";
        dis = " ";
        routemodule = false;

        start_route_button.setEnabled(false);
        stop_route_button.setEnabled(false);
        points = new ArrayList<>();
        route = new ArrayList<>();

        init();
        restoreValuesFromBundle(savedInstanceState);
        //start();

    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                if (first) {
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
        double d = Math.round(wholeDistance);
        dis = Double.toString(d);
        //dis = String.format("%.2f", wholeDistance);
/*
        velocity = computeVelocity(mOldLocation, mCurrentLocation, distance);
        vel = String.format("%.2f", velocity);*/
    }
/*
    private double computeVelocity(Location mOldLocation, Location mCurrentLocation, double distance) {


        double speed = distance / (mCurrentLocation.getTime() - mOldLocation.getTime());
        if (mCurrentLocation.hasSpeed()) {
            speed = mCurrentLocation.getSpeed();
        }
        speed = speed * 3.6;
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

            if (savedInstanceState.containsKey("distance")) {
                distance = savedInstanceState.getDouble("distance");
            }

            if (savedInstanceState.containsKey("velocity")) {
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

            if (routemodule) {
                points.add(place);
                redrawLine();
            }
            mMap.addMarker(new MarkerOptions().position(place));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        }

    }

    private void redrawLine() {
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        //Polyline line = mMap.addPolyline(options);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putDouble("distance", distance);
        outState.putDouble("velocity", velocity);

    }

    public void startLocationClick(View view) {

        start();
    }
    public void start() {

        mRequestingLocationUpdates = lp.startClick(this);
        if (mRequestingLocationUpdates) {
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

    int index;
    int time;

    private void getInfoFromAPI(double lat1, double lon1, double lat2, double lon2) {

        String l1 = Double.toString(lat1);
        String l2 = Double.toString(lon1);
        String l3 = Double.toString(lat2);
        String l4 = Double.toString(lon2);

        RequestQueue queue = Volley.newRequestQueue(RouteMapsActivity.this);
        /*String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
                Double.toString(lat1) + "," +
                Double.toString(lon1) + "," + "&destinations=" +
                Double.toString(lat2) + "," +
                Double.toString(lon2) + "&departure_time=now&key=AIzaSyCjWmzamhQBA4Z9ZEwT4r8bt3G7G1hVXjc";*/
       // String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=51.109731,17.053735&destinations=52.20588,21.039098&departure_time=now&key=AIzaSyCjWmzamhQBA4Z9ZEwT4r8bt3G7G1hVXjc";
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + l1 + "," + l2 +
                "&destinations=" + l3 + "," + l4 +
                "&departure_time=now&key=AIzaSyCjWmzamhQBA4Z9ZEwT4r8bt3G7G1hVXjc";
        //String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + l1 + ",16.0494699&destinations=50.7822707,16.0495779&departure_time=now&key=AIzaSyCjWmzamhQBA4Z9ZEwT4r8bt3G7G1hVXjc";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response).getJSONArray("rows")
                                    .getJSONObject(0)
                                    .getJSONArray ("elements")
                                    .getJSONObject(0)
                                    .getJSONObject("duration_in_traffic");
                            String distance = jsonObject.get("value").toString();
                            int time = Integer.parseInt(distance);
                            time = time / 60;
                            int secs = time % 60;
                            String res = "Przewidywany czas przejazdu z Google Maps wynosi: " + String.valueOf(time) +
                                    " min " + String.valueOf(secs) + " s";

                            Toast.makeText(RouteMapsActivity.this, res, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RouteMapsActivity.this,  "Brak połączenia z Internetem", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }

    @SuppressLint("SetTextI18n")
    public void startRouteClick(View view) {
        route = fetchDataFromDB(routeName);
        time_table  = fetchTimeFromDB(routeName);
        coordinates_table = fetchCoordinatesFromDB(routeName);

        time = time * 1000;
        start_route_button.setEnabled(false);
        stop_route_button.setEnabled(true);
        distance = wholeDistance = velocity = 0;
        index = 1;
        distance_field.setText("Dystans: 0,00 / " + route.get(route.size()-1) + "m");
        getInfoFromAPI(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), coordinates_table.get(coordinates_table.size()-2), coordinates_table.get(coordinates_table.size()-1));
        runnable3.run();
        //int t2 = getInfoFromAPI(coordinates_table.get(2), coordinates_table.get(3), coordinates_table.get(4), coordinates_table.get(5));
       // Toast.makeText(RouteMapsActivity.this, dupsko, Toast.LENGTH_SHORT).show();


    }



    private int timeleft(double travelledDistance) {

        int i = 0;
        int j = 1;
        int index = 0;

        while(j < route.size()) {
            // metoda dis oblicza i zwraca różnicę między dwoma argumentami
            if ( dis(route.get(i), travelledDistance) <= dis(route.get(j), travelledDistance)) {

                index = i;
                break;
            }
            i++;
            j++;

            if (j == route.size() - 1) {
                index = j;
            }
        }

        return time_table.get(time_table.size()-1) - time_table.get(index);
    }

    private double dis(double distance, double travelledDistance) {
        return Math.abs(distance - travelledDistance);
    }


int counter = 0;
    private Runnable runnable3  = new Runnable() {
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        public void run() {
            counter++;
            //String totalDistance = String.format("%.2f", route.get(route.size()-1));
            double d = Math.round(route.get(route.size()-1));
            String totalDistance = Double.toString(d);
            if (wholeDistance > route.get(route.size()-1)) {
                distance_field.setText("Dojechałeś do celu");
                time_left_field.setText("Czas minął");
            }
            else {
                distance_field.setText("Dystans: " +  dis + " m / " + totalDistance + " m");
                distance_field.setAlpha(0);
                distance_field.animate().alpha(1).setDuration(300);

                int timeresult = timeleft(wholeDistance);
                int mins = timeresult / 60;
                int secs = mins % 60;
                String res = Integer.toString(mins) + " min " + Integer.toString(secs) + " s";
                time_left_field.setText("Zostało " + res);
                time_left_field.setAlpha(0);
                time_left_field.animate().alpha(1).setDuration(300);
                if (counter == 6) {
                    getInfoFromAPI(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), coordinates_table.get(coordinates_table.size()-2), coordinates_table.get(coordinates_table.size()-1));
                    counter = 0;
                }
            }

/*
            double diff;
            //String diffstring;


            if(index < route.size()) {

                if(wholeDistance < route.get(index)) {

                    diff = route.get(index) - wholeDistance;
                    double velocity = diff / 10;
                    Toast.makeText(RouteMapsActivity.this, "Jedz z predkoscia " + Double.toString(velocity) + " m/s" , Toast.LENGTH_SHORT).show();

                }
                else {

                    int m = 2;
                    int tmp = index + 1;

                    if(tmp < route.size()) {
                        while (wholeDistance >= route.get(tmp)) {
                            m++;
                            tmp++;
                            if(tmp >= route.size()) {
                                Toast.makeText(RouteMapsActivity.this, "Dojechales znacznie przed czasem", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }

                    }
                    else {
                        Toast.makeText(RouteMapsActivity.this, "Dojechales chwile przed czasem", Toast.LENGTH_SHORT).show();
                    }

                    if (tmp < route.size()) {

                        diff = route.get(tmp) - wholeDistance;
                        velocity = diff / (10 * m);
                        Toast.makeText(RouteMapsActivity.this, "Jedz z predkoscia " + Double.toString(velocity) + " m/s" , Toast.LENGTH_SHORT).show();

                    }

                }

                index++;
            }
            else {
                Toast.makeText(RouteMapsActivity.this, "Koniec trasy" , Toast.LENGTH_SHORT).show();
                //cos w stylu zakoncz dzialanie
            }
*/
            handler.postDelayed(this, 10000);

        }
    };

    private ArrayList fetchDataFromDB(String routeName) {

        String fromDB = databaseHelper.getJsonfromDB(routeName);
        Type listType = new TypeToken<ArrayList<Double>>(){}.getType();
        return new Gson().fromJson(fromDB, listType);
    }

    private ArrayList fetchTimeFromDB(String routeName) {

        String fromDB = databaseHelper.getTimeFromDB(routeName);
        Type listType = new TypeToken<ArrayList<Integer>>(){}.getType();
        return new Gson().fromJson(fromDB, listType);
    }

    private ArrayList fetchCoordinatesFromDB(String routeName) {
        String fromDB = databaseHelper.getCoordinatesFromDB(routeName);
        Type listType = new TypeToken<ArrayList<Double>>(){}.getType();
        return new Gson().fromJson(fromDB, listType);
    }
int d = 0;
    public void stopRouteClick(View view) {

        handler.removeCallbacks(runnable3);
       // handler.removeCallbacks(runnable2);
        //timeSwapBuff = 0;
        distance = wholeDistance = velocity = 0.0;
    }
}
 //