package gruppe11.aufgabe_2.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.HttpURLConnection;
import java.util.Map;

import gruppe11.aufgabe_2.R;
import gruppe11.aufgabe_2.map_items.CommunityItem;
import gruppe11.aufgabe_2.map_items.LocalizableService;
import gruppe11.aufgabe_2.map_items.User;
import gruppe11.aufgabe_2.rest.Event;
import gruppe11.aufgabe_2.rest.RestEvent;
import gruppe11.aufgabe_2.rest.RestService;
import gruppe11.aufgabe_2.rest.UpdateService;

import gruppe11.aufgabe_2.utility.Utility;

/**
 * Main Activity displaying an instance of gmaps with markers indicating the client's/community's
 * current whereabouts as well as marking (red circle) the area in which said items will be displayed.
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String DEBUGLOG_TAG = "DEBUGLOG-MA";
    private GoogleMap mMap;
    private Menu menu = null;
    private GoogleApiClient mGoogleApiClient = null;
    private Location mLastLocation = null;
    private static final int MARKER_HUE = 200;
    private static final int MAP_PADDING = 50;
    private UpdateService updateService = null;
    private RestService restService = null;
    private LatLng latLng = null;
    private Circle circle = null;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.d(DEBUGLOG_TAG, "init mGoogleApiClient");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Utility.refreshEventBus(this);

        if (updateService == null) {
            Intent intent = new Intent(this, UpdateService.class);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }

        if (restService == null) {
            Intent intent2 = new Intent(this, RestService.class);
            bindService(intent2, serviceConnection2, BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);

        for (int i = 0; i < menu.size(); i++) {

            int id = menu.getItem(i).getItemId();
            if (restService.isLoggedIn()) {
                if (id == R.id.menu_login || id == R.id.menu_register) {
                    menu.getItem(i).setVisible(false);
                } else if (id == R.id.menu_logout || id == R.id.menu_config_account) {
                    menu.getItem(i).setVisible(true);
                }
            } else {
                if (id == R.id.menu_login || id == R.id.menu_register) {
                    menu.getItem(i).setVisible(true);
                } else if (id == R.id.menu_logout || id == R.id.menu_config_account) {
                    menu.getItem(i).setVisible(false);
                }
            }
        }

        return true;
    }

    /**
     * Event Bus event receiver for RestEvent
     *
     * @param restEvent Change in RestEvent class
     */
    public void onEvent(RestEvent restEvent) {

        Event event = restEvent.getEvent();
        int responseCode;

        switch (event) {
            case LOGIN:
                Log.d(DEBUGLOG_TAG, "LoginEvent Received");
                populateOptionsMenuItems(this.menu);
                responseCode = restEvent.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {//200
                    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
                    restService.getCommunityData(Integer.valueOf(sPref.getString("radius", "25")));
                    if (updateService != null) {
                        updateService.start(this);
                    }
                }
                break;

            case LOGOUT:
                Log.d(DEBUGLOG_TAG, "LogoutEvent Received");

                Toast.makeText(MapsActivity.this, "Logged out", Toast.LENGTH_LONG).show();
                populateOptionsMenuItems(this.menu);
                if (updateService != null) {
                    updateService.stop();
                }
                if (mLastLocation != null) {
                    setClientPosition();
                    updateMarkers();
                } else {
                    Log.d(DEBUGLOG_TAG, "mLastLocation null");
                }
                break;

            case REGISTER:
                Log.d(DEBUGLOG_TAG, "RegisterEvent Received");
                break;

            case UPDATE:
                Log.d(DEBUGLOG_TAG, "UpdateEvent Received");
                setClientPosition();
                if (restService != null) {
                    if (restService.isLoggedIn()) {
                        restService.updateClientGPSData();
                        LocalizableService.getInstance().clearCommunityItems();
                        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
                        restService.getCommunityData(Integer.valueOf(sPref.getString("radius", "25")));
                    }
                }
                break;

            case COMMUNITY:
                Log.d(DEBUGLOG_TAG, "CommunityGPSDATA Event Received");
                responseCode = restEvent.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {//200
                    updateMarkers();
                }
                break;
        }

        Utility.refreshEventBus(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.menu_register:
                Log.d(DEBUGLOG_TAG, "selected menu item: Register");
                if (checkLocationPermissions()) {
                    intent = new Intent(this, RegisterActivity.class);
                    intent.putExtra("restService", restService);
                    startActivity(intent);
                }
                return true;

            case R.id.menu_login:
                Log.d(DEBUGLOG_TAG, "selected menu item: Login");
                if (checkLocationPermissions()) {
                    intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("restService", restService);
                    startActivity(intent);
                }
                return true;

            case R.id.menu_logout:
                Log.d(DEBUGLOG_TAG, "selected menu item: Logout");
                restService.logoutClient();
                populateOptionsMenuItems(this.menu);
                return true;

            case R.id.menu_config_account:
                Log.d(DEBUGLOG_TAG, "selected menu item: Config Account");
                intent = new Intent(this, ConfigureAccountActivity.class);
                intent.putExtra("restService", restService);
                startActivity(intent);
                return true;

            case R.id.menu_settings:
                Log.d(DEBUGLOG_TAG, "selected menu item: Setting");
                intent = new Intent(this, PreferencesActivity.class);
                intent.putExtra("updateService", updateService);
                intent.putExtra("restService", restService);
                startActivity(intent);
                return true;

            default:
                Log.d(DEBUGLOG_TAG, "(selected menu item) Unexpected Condition");
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Checks client ACCESS_FINE_LOCATION permission
     *
     * @return true
     * permissions granted
     * false
     * permissions lacking
     */
    public boolean checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return false;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(DEBUGLOG_TAG, "connection with GMS successful");
        setClientPosition();
        updateMarkers();
        if (circle != null) {
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(Utility.toBounds(circle.getCenter(), circle.getRadius()), MAP_PADDING);
            mMap.moveCamera(cu);
        }
//      Update on start-up
//        if(updateService != null){
//            updateService.start(this);
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(DEBUGLOG_TAG, "connection with GMS services suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(DEBUGLOG_TAG, "connection with GMS services failed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    Log.d(DEBUGLOG_TAG, "RequestPermissionResult: Permission granted for FINE Location");
                    setClientPosition();
                    updateMarkers();
                    if (circle != null) {
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(Utility.toBounds(circle.getCenter(), circle.getRadius()), MAP_PADDING);
                        mMap.moveCamera(cu);
                    }
                } else {
                    // permission denied
                    Log.d(DEBUGLOG_TAG, "RequestPermissionResult: Permission denied for FINELocation");
                    Toast.makeText(MapsActivity.this, "Permission required to fully utilize the application", Toast.LENGTH_LONG).show();


                }
        }
    }

    /**
     * Sets markers according to latitude / longitude values of Localizable Objects saved in
     * LocalizableService.LIST_OF_LOCALIZABLES
     */
    private void updateMarkers() {
        Log.d(DEBUGLOG_TAG, "called updateMarkers");
        mMap.clear();
        for (int i = 0; i < LocalizableService.getInstance().getSize(); i++) {
            latLng = new LatLng(LocalizableService.getInstance().getLocalizable(i).getLatitude(), LocalizableService.getInstance().getLocalizable(i).getLongitude());
            String description = "";
            String username = "";
            if (LocalizableService.getInstance().getLocalizable(i) instanceof User) {

                if (((User) LocalizableService.getInstance().getLocalizable(i)).getUsername() != null) {
                    username = ((User) LocalizableService.getInstance().getLocalizable(i)).getUsername();
                }

                if (((User) LocalizableService.getInstance().getLocalizable(i)).getDescription() != null) {
                    description = ((User) LocalizableService.getInstance().getLocalizable(i)).getDescription();
                }

                if (restService.isLoggedIn()) {
                    if (description.isEmpty()) {
                        mMap.addMarker(new MarkerOptions()
                                .zIndex(1)
                                .position(latLng)
                                .title(username));
                    } else {
                        mMap.addMarker(new MarkerOptions()
                                .zIndex(1)
                                .position(latLng)
                                .title(username)
                                .snippet(description));
                    }
                } else {
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Me")
                            .snippet("offline"));

                }
                //set circle - for client only
                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
                Log.d(DEBUGLOG_TAG, "Radius value MA: " + sPref.getString("radius", "25"));
                int radiusInMeters = Integer.valueOf(sPref.getString("radius", "25")) * 1000;
                CircleOptions circleOptions = new CircleOptions()
                        .center(latLng)
                        .strokeColor(Color.RED)
                        .radius(radiusInMeters);
                circle = mMap.addCircle(circleOptions);
            } else {
                if (((CommunityItem) LocalizableService.getInstance().getLocalizable(i)).getName() != null) {
                    username = ((CommunityItem) LocalizableService.getInstance().getLocalizable(i)).getName();
                }
                if (((CommunityItem) LocalizableService.getInstance().getLocalizable(i)).getDescription() != null) {
                    description = ((CommunityItem) LocalizableService.getInstance().getLocalizable(i)).getDescription();
                }

                if (description.isEmpty()) {
                    mMap.addMarker(new MarkerOptions()
                            .zIndex(0)
                            .position(latLng)
                            .title(username)
                            .icon(BitmapDescriptorFactory.defaultMarker(MARKER_HUE)));
                } else {
                    mMap.addMarker(new MarkerOptions()
                            .zIndex(0)
                            .position(latLng)
                            .title(username)
                            .snippet(description)
                            .icon(BitmapDescriptorFactory.defaultMarker(MARKER_HUE)));
                }
            }
        }
    }

    /**
     * Fetches current location provided by the GPS module and stores
     * current latitude / longitude values in client object.
     */
    private void setClientPosition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(DEBUGLOG_TAG, "Permission granted. Return current location");
            if (LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) == null) {
                Log.d(DEBUGLOG_TAG, "LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) returns null");
            } else {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                Log.d(DEBUGLOG_TAG, "mLastLocation - latitude: " + mLastLocation.getLatitude());
                Log.d(DEBUGLOG_TAG, "mLastLocation - longitude: " + mLastLocation.getLongitude());
                if (LocalizableService.getInstance().getClientLocalizable() != null) {
                    LocalizableService.getInstance().getClientLocalizable().setLatitude(mLastLocation.getLatitude());
                    LocalizableService.getInstance().getClientLocalizable().setLongitude(mLastLocation.getLongitude());
                } else {
                    LocalizableService.getInstance().add(new User(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                }
            }
        } else {
            Log.d(DEBUGLOG_TAG, "No ACCESS_FINE_LOCATION Permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    /**
     * Updates memu according to the current state of the application and the client (logged in / logged out)
     *
     * @param menu Toolbar menu
     */
    public void populateOptionsMenuItems(Menu menu) {
        Log.d(DEBUGLOG_TAG, "Populating options menu");
        for (int i = 0; i < menu.size(); i++) {

            int id = menu.getItem(i).getItemId();
            if (restService.isLoggedIn()) {
                if (id == R.id.menu_login || id == R.id.menu_register) {
                    menu.getItem(i).setVisible(false);
                } else if (id == R.id.menu_logout || id == R.id.menu_config_account) {
                    menu.getItem(i).setVisible(true);
                }
            } else {
                if (id == R.id.menu_login || id == R.id.menu_register) {
                    menu.getItem(i).setVisible(true);
                } else if (id == R.id.menu_logout || id == R.id.menu_config_account) {
                    menu.getItem(i).setVisible(false);
                }
            }
        }

        invalidateOptionsMenu();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(DEBUGLOG_TAG, "Service connected");
            UpdateService.LocalService localService = (UpdateService.LocalService) service;
            updateService = localService.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            updateService = null;
        }
    };


    private ServiceConnection serviceConnection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(DEBUGLOG_TAG, "Service connected");
            RestService.LocalService localService = (RestService.LocalService) service;
            restService = localService.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            restService = null;
        }
    };
}