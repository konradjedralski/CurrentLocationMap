package com.example.moje.currentlocationmap;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code = 99;
    private static final int Request_User_SMS_Code = 88;
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            checkUserLocationPermission();
            statusNetworkCheck();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Method checks if map is ready to use, then get user location.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            buildGoogleApiClient();

            mMap.setMyLocationEnabled(true);
        }

    }

    /**
     * Method checks user location permission.
     * @return
     * true if user allowed location permission<br>
     * false if user did not allowed location permission
     */
    public boolean checkUserLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        } else {
            statusLocationCheck();
            return true;
        }
    }

    /**
     * Method checks user SMS (message) permission.
     * @return
     * true if user allowed SMS permission<br>
     * false if user did not allowed SMS permission
     */
    public boolean checkUserSMSPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, Request_User_SMS_Code);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, Request_User_SMS_Code);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Method is handling the permission request response.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (googleApiClient == null) {

                            buildGoogleApiClient();
                        }

                        mMap.setMyLocationEnabled(true);
                    }
                    statusLocationCheck();
                } else {

                    Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
                }
                return;

            case Request_User_SMS_Code:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "SMS permission denied!", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
    /**
     * Method is building Google Api Client.
     */
    protected synchronized void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    /**
     * Method is replacing marker if location is changed.
     */
    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;

        if (currentUserLocationMarker != null) {

            currentUserLocationMarker.remove();
        }
        latitude = Double.toString(location.getLatitude());
        longitude = Double.toString(location.getLongitude());
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Your Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentUserLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera((CameraUpdateFactory.newLatLng(latLng)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));


        if (googleApiClient != null) {

            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    /**
     * Method is updating location.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Method redirects to sending a message with current location after button click.
     */
    public void buttonOnClick(View V) {

        Intent myIntent = new Intent(Intent.ACTION_SEND);
        myIntent.setType("text/plain");

        if (latitude != null && longitude != null) {

            if (checkUserSMSPermission() == true) {
                String shareBody = "My current location is: \n" + latitude + ", " + longitude;
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(myIntent, "Send your location"));
            }
        } else {

            Toast.makeText(this, "Location permission denied or location is disabled!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method checks if the location is on.
     */
    public void statusLocationCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    /**
     * Method checks if the network is on.
     */
    public void statusNetworkCheck() {
        final ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (!manager.isDefaultNetworkActive()) {
            buildAlertMessageNoNetwork();

        }
    }

    /**
     * Method displays a message about the switched off location and allows it to be enabled in the settings.
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your location seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Method displays a message about the switched off network and allows it to be enabled in the settings.
     */
    private void buildAlertMessageNoNetwork() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your mobile data seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
