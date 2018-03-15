package mobileprogramming.testassignment.activity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import mobileprogramming.testassignment.R;
import mobileprogramming.testassignment.event.GeoFenceEvent;
import mobileprogramming.testassignment.event.NetworkChangeEvent;
import mobileprogramming.testassignment.service.GeofenceTransitionService;
import mobileprogramming.testassignment.utils.Constant;
import mobileprogramming.testassignment.utils.TestApplication;
import mobileprogramming.testassignment.utils.UtilityMethods;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Landing Activity for map and config controls
 */
public class LandingActivity extends Activity implements
        View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    private static final String TAG = LandingActivity.class.getSimpleName();
    private static final String[] permissionsList = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private TextView tvLat;
    private TextView tvEvent;
    private TextView tvLong;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Marker geoFenceMarker;
    private Marker locationMarker;
    private Circle geoFenceLimits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        //initialize views
        initView();

        //initialize google maps
        initGoogleMaps();

        //set network preference for initial network info
        UtilityMethods.setNetworkPrefrence(this);
    }

    /**
     * Event subscriber for Geofence enter/exit event
     *
     * @param event Enter/Exit
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final GeoFenceEvent event) {
        String eventName = event.getEventName();
        Log.i(TAG, "onMessageEvent: " + eventName);
        tvEvent.setText(eventName);
        if (eventName.contains("Enter")) {
            tvEvent.setBackgroundColor(Color.GREEN);
        } else {
            if (!checkForNetworkSwitch()) {
                tvEvent.setBackgroundColor(Color.RED);
                UtilityMethods.setNetworkPrefrence(this);
            } else {
                tvEvent.setText(R.string.exit_event_with_same_network);
            }
        }
    }

    /**
     * Used to check network switch with initial and current network info
     *
     * @return true if same else false
     */
    private boolean checkForNetworkSwitch() {
        NetworkInfo networkInfo = UtilityMethods.getNetworkInfo(this);
        TestApplication testApplication = (TestApplication) getApplication();
        return networkInfo.getExtraInfo().equalsIgnoreCase(testApplication.getNetworkName()) && networkInfo.getTypeName().equalsIgnoreCase(testApplication.getNetworkType());
    }

    /**
     * Event subscriber for Network switch event
     *
     * @param event NetworkChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final NetworkChangeEvent event) {
        if (event != null && tvEvent.getText().toString().contains("Exit")) {
            tvEvent.setBackgroundColor(Color.RED);
            UtilityMethods.setNetworkPrefrence(LandingActivity.this);
        }
    }

    /**
     * Create GoogleApiClient instance
     */
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /**
     * Initialize GoogleMaps
     */
    private void initGoogleMaps() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Trigger new location updates at interval
     */
    protected void startLocationUpdates() {
        if (!UtilityMethods.checkLocationPermission(this)) {
            // Create the location request to start receiving updates
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(Constant.UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(Constant.FASTEST_INTERVAL);

            // Create LocationSettingsRequest object using location request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            // Check whether location settings are satisfied
            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(locationSettingsRequest);


            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    Looper.myLooper());
        } else {
            UtilityMethods.requestPermissions(LandingActivity.this, permissionsList);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        //check for GPS settings
        checkForGPS();
        // Call GoogleApiClient connection when starting the Activity
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        // Disconnect GoogleApiClient when stopping Activity
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 999) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                startLocationUpdates();
            } else {
                // Permission denied
                UtilityMethods.permissionsDenied(LandingActivity.this);
            }
        }
    }

    /**
     * Used to get location updates
     * @param location Location Object
     */
    public void onLocationChanged(Location location) {
        writeActualLocation(location);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        markerLocation(latLng);
    }

    /**
     * Update lat long on UI and marker
     * @param location Location Object
     */
    private void writeActualLocation(Location location) {
        tvLat.setText(String.format("Lat: %s", location.getLatitude()));
        tvLong.setText(String.format("Long: %s", location.getLongitude()));
        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    /**
     * Used to create marker for current location and set camera focus
     * @param latLng current location
     */
    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if (map != null) {
            if (locationMarker != null) locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }

    /**
     * Used to check GPS settings
     */
    private void checkForGPS() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        if (!enabled) {
            Toast.makeText(this, R.string.location_services_info, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            // create GoogleApiClient
            createGoogleApi();
        }
    }

    /**
     * Init views associated with activity
     */
    private void initView() {
        tvLat = findViewById(R.id.lat);
        tvEvent = findViewById(R.id.tv_event);
        tvLong = findViewById(R.id.lon);
        Button btnCreateGeofence = findViewById(R.id.btn_create_geofence);
        btnCreateGeofence.setOnClickListener(this);
        Button btnclearGeofence = findViewById(R.id.btn_clear_geofence);
        btnclearGeofence.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create_geofence:
                createGeofence();
                break;
            case R.id.btn_clear_geofence:
                clearGeofence();
                break;
            default:
        }
    }

    /**
     * Used to clear created GeoFence
     */
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    // remove drawing
                    clearGeofenceDraw();
                }
            }
        });
    }

    /**
     * clear geofence draw
     */
    private void clearGeofenceDraw() {
        Log.d(TAG, "clearGeofenceDraw()");
        if (geoFenceMarker != null) {
            geoFenceMarker.remove();
        }
        if (geoFenceLimits != null) {
            geoFenceLimits.remove();
        }
    }

    /**
     * Used to create GeoFence
     */
    private void createGeofence() {
        Log.i(TAG, "createGeofence()");
        if (geoFenceMarker != null) {
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), Constant.GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    /**
     * Create a Geofence
     * @param latLng LatLong Object
     * @param radius Radious of geofence
     * @return GeoFence Object
     */
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(Constant.GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Constant.GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    /**
     * Create a Geofence Request
     * @param geofence GeoFence Onbject
     * @return GeoFencingRequest Object
     */
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    /**
     * createGeofencePendingIntent
     * @return PendingIntent
     */
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        Intent intent = new Intent(this, GeofenceTransitionService.class);
        int GEOFENCE_REQ_CODE = 0;
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Add the created GeofenceRequest to the device's monitoring list
     * @param request GeofencingRequest
     */
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");

        if (UtilityMethods.checkLocationPermission(this)) {
            UtilityMethods.requestPermissions(LandingActivity.this, permissionsList);
        } else {
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        markerForGeofence(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    /**
     * Mareker creation for geofence
     * @param latLng LatLng
     */
    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if (map != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null) geoFenceMarker.remove();
            geoFenceMarker = map.addMarker(markerOptions);

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        startLocationUpdates();
    }

    /**
     * draw geofence
     */
    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if (geoFenceLimits != null) {
            geoFenceLimits.remove();
        }

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(Constant.GEOFENCE_RADIUS);
        geoFenceLimits = map.addCircle(circleOptions);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            saveGeofence();
            drawGeofence();
        } else {
            Toast.makeText(this, "Geofence add request fails", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saving GeoFence marker with prefs mng
     */
    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
        editor.putLong(KEY_GEOFENCE_LAT, Double.doubleToRawLongBits(geoFenceMarker.getPosition().latitude));
        String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";
        editor.putLong(KEY_GEOFENCE_LON, Double.doubleToRawLongBits(geoFenceMarker.getPosition().longitude));
        editor.apply();
    }
}
