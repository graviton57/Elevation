package com.havrylyuk.elevation.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.havrylyuk.elevation.BuildConfig;
import com.havrylyuk.elevation.R;
import com.havrylyuk.elevation.adapter.CustomInfoWindowAdapter;
import com.havrylyuk.elevation.dialog.SourcesDialog;
import com.havrylyuk.elevation.events.ElevationEvent;
import com.havrylyuk.elevation.service.ElevationService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 *
 * Created by Igor Havrylyuk on 25.03.2017.
 */
public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient googleApiClient;
    // A default developer location (Chernivtsi, Ukraine) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(48.2917, 25.9352);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean isLocationPermissionGranted;
    // The geographical location where the device is currently located.
    private Location currentLocation;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "com.havrylyuk.elevation.camera_position";
    private static final String KEY_LOCATION = "com.havrylyuk.elevation.location";
    private Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        // Build the Play services client for use by the Fused Location Provider and the Locations API.
        buildGoogleApiClient();
        googleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_clear_map:
                    if (map != null) {
                        map.clear();
                    }
                    return true;
                case R.id.navigation_settings:
                    SourcesDialog sourcesDialog = SourcesDialog.newInstance();
                    sourcesDialog.show(getSupportFragmentManager(), SourcesDialog.SOURCE_DIALOG_TAG);
                    return true;
                case R.id.navigation_share:
                    shareData();
                    return true;
            }
            return false;
        }
    };

    /**
     * Get the device location and nearby places when the activity is restored after a pause.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            getDeviceLocation();
        }
        updateMarkers();
    }

    /**
     * Stop location updates when the activity is no longer in focus, to reduce battery consumption.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, currentLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Gets the device's current location and builds the map
     * when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        getDeviceLocation();
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(LOG_TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(LOG_TAG, "Play services connection suspended");
    }

    /**
     * Handles the callback when location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        updateMarkers();
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        updateLocationUI();
        updateMarkers();
        this.map.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
        this.map.setOnMapClickListener(this);
        this.map.setOnMarkerClickListener(this);
        if (cameraPosition != null) {
            this.map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else if (currentLocation != null) {
            this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(LOG_TAG, "Current location is null. Using defaults.");
            this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
            this.map.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /**
     * Builds a GoogleApiClient.
     * Uses the addApi() method to request the Google Places API and the Fused Location Provider.
     */
    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Gets the current location of the device and starts the location update notifications.
     */
    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (isLocationPermissionGranted) {
            currentLocation = LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        isLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Adds markers for places nearby the device and turns the My Location feature on or off,
     * provided location permission has been granted.
     */
    private void updateMarkers() {
        if (map == null) {
            return;
        }
        if (isLocationPermissionGranted) {
            if (BuildConfig.DEBUG) Log.d(LOG_TAG, "add Your Location Marker");
            if (currentLocation != null) {
                addLocationMarker(currentLocation.getLatitude(),currentLocation.getLongitude());
            }
        } else {
            if (BuildConfig.DEBUG) Log.d(LOG_TAG, "add Developer Location Marker");
            addLocationMarker(defaultLocation.latitude,defaultLocation.longitude);
        }
    }

    /**
     * Start fetching elevation from network.
     */
    public void addLocationMarker( double latitude, double longitude) {
        startLocationService(this, new LatLng(latitude, longitude));
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        if (isLocationPermissionGranted) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            currentLocation = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(ElevationEvent event) {
        Log.d(LOG_TAG, "ElevationEvent elevation=" + event.getElevation());
        addMarkerOnUserClick(event.getLatLng(), event.getElevation(), event.getLocation());
        if (currentLocation != null) {
            Log.d(LOG_TAG, "currentLocation.getAltitude()=" + currentLocation.getAltitude());
        }

    }

    /**
     * Start Geo Coder Intent Service.
     */
    private void startLocationService(Context context, LatLng latLng){
        // Determine whether a Geocoder is available.
        if (!Geocoder.isPresent()) {
            Toast.makeText(context, R.string.error_no_geocoder_available, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, ElevationService.class);
        intent.putExtra(ElevationService.LATLNG_DATA_EXTRA, latLng);
        context.startService(intent);
    };

    /**
     * On user click on map start fetching data.
     */
    @Override
    public void onMapClick(LatLng latLng) {
        startLocationService(this,latLng);
    }

    /**
     * Add marker on user click on map.
     */
    private void addMarkerOnUserClick(LatLng latLng , String title, String snippet){
        if (latLng != null) {
            IconGenerator iconFactory = new IconGenerator(this);
            iconFactory.setRotation(90);
            iconFactory.setContentRotation(-90);
            iconFactory.setStyle(IconGenerator.STYLE_GREEN);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(getString(R.string.format_elevation_title, title));
            markerOptions.snippet(snippet);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(title)))
                         .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
            if (map != null) {
                Marker userMarker = map.addMarker(markerOptions);
                userMarker.showInfoWindow();
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        } else {
            Toast.makeText(this, R.string.error_add_marker,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Change selectedMarker value .
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        selectedMarker = marker;
        return false;
    }

    /**
     * Share selectedMarker data (elevation, location) .
     */
    @SuppressWarnings("deprecation")
    private void shareData() {
        if (selectedMarker != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            } else {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            }
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, selectedMarker.getTitle()+
                    " Latitude:"+selectedMarker.getPosition().latitude+
                    " Longitude:"+selectedMarker.getPosition().longitude+
                    " "+selectedMarker.getSnippet());
            startActivity(shareIntent);
        } else {
            Toast.makeText(this, R.string.select_marker_first,Toast.LENGTH_SHORT).show();
        }
    }
}
