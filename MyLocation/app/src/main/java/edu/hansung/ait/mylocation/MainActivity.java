package edu.hansung.ait.mylocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    final private String TAG = "LocationServicesTest";
    final private int MY_PERMISSION_REQUEST_LOCATION = 100;

    // UI Widgets.
    private Button mStartUpdatesButton;
    private Button mStopUpdatesButton;
    private TextView mAddressTextView;
    private TextView mPrecisionTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationListener mLocationListener;
    private boolean mRequestingLocationUpdates;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private float precision= 0.0f;

    private int sw = 0;
    private int mSw = 0;

    private GoogleMap googleMap;
    public Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mPrecisionTextView = (TextView) findViewById(R.id.precision_text);
        mAddressTextView = (EditText) findViewById(R.id.address_text);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new MyConnectionCallBack())
                    .addOnConnectionFailedListener(new MyOnConnectionFailedListener())
                    .addApi(LocationServices.API)
                    .build();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        googleMap.clear();

        // Add a marker in Sydney and move the camera
        googleMap.addMarker(new MarkerOptions().position(latLng).title("Marker in click location"));
    }

    private class MyConnectionCallBack implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle bundle) {
            Log.i(TAG,"onConnected");
            if (isPermissionGranted())
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateUI();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(TAG,"onConnectionSuspended");
        }
    }

    private boolean isPermissionGranted() {
        String[] PERMISSIONS_STORAGE = {    // 요청할 권한 목록을 설정
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,			// MainActivity 액티비티의 객체 인스턴스를 나타냄
                    PERMISSIONS_STORAGE,        // 요청할 권한 목록을 설정한 String 배열
                    MY_PERMISSION_REQUEST_LOCATION    // 사용자 정의 int 상수. 권한 요청 결과를 받을 때
            );
            return false;
        }
        else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    updateUI();
                }
                else
                    Toast.makeText(this,"Permission required",Toast.LENGTH_SHORT);
            }
        }
    }

    private class MyOnConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.i(TAG,"onConnectionFailed");
        }
    }

    @Override
    protected void onStart() {
        Log.i(TAG,"onStart, connect request");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates && mGoogleApiClient.isConnected() && isPermissionGranted())
            startLocationUpdates();
    }

    // It is a good practice to remove location requests when the activity is in a paused or
    // stopped state. Doing so helps battery performance and is especially
    // recommended in applications that request frequent location updates.
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected())
            stopLocationUpdates();
    }

    // It is a good practice to remove location requests when the activity is in a paused or
    // stopped state. Doing so helps battery performance and is especially
    // recommended in applications that request frequent location updates.
    @Override
    protected void onStop() {
        Log.i(TAG,"onStop, disconnect request");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
            precision = mCurrentLocation.getAccuracy();
        }
        mLatitudeTextView.setText("Latitude: "+ latitude);
        mLongitudeTextView.setText("Longitude: "+ longitude);
        mPrecisionTextView.setText("Precision: "+ precision);
    }

    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            if (mGoogleApiClient.isConnected() && isPermissionGranted()) {
                sw = 1;
                mRequestingLocationUpdates = true;
                setButtonsEnabledState();
                startLocationUpdates();

                try {
                    Geocoder geocoder = new Geocoder(this, Locale.KOREA);
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
/*                      mAddressTextView.setText(String.format("\n[%s]\n[%s]\n[%s]\n[%s]",
                                address.getFeatureName(),
                                address.getThoroughfare(),
                                address.getLocality(),
                                address.getCountryName()
                        ));*/

                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(this);
                    }
                }
                catch (IOException e) {
                    Log.e(TAG, "Failed in using Geocoder", e);
                }
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locRequest = new LocationRequest();
        locRequest.setInterval(10000);
        locRequest.setFastestInterval(5000);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mCurrentLocation = location;
                latitude = mCurrentLocation.getLatitude();
                longitude = mCurrentLocation.getLongitude();
                updateUI();
            }
        };

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locRequest, mLocationListener);
    }

    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    private  void stopLocationUpdates() {
        if (mLocationListener != null && mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
    }

    private void setButtonsEnabledState() {
        if(sw != 0) {
            if (mRequestingLocationUpdates) {
                mStartUpdatesButton.setEnabled(false);
                mStopUpdatesButton.setEnabled(true);
            } else {
                mStartUpdatesButton.setEnabled(true);
                mStopUpdatesButton.setEnabled(false);
            }
        }
    }

    public void getAddressButtonHandler(View view) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.KOREA);
            List<Address> addresses = geocoder.getFromLocationName(mAddressTextView.getText().toString(), 3);
            if (addresses.size() > 0) {
                for (int i = 0; i < addresses.size(); i++) {
                    sw = 2;
                    Address outAddr = addresses.get(i);
                    int addrCount = outAddr.getMaxAddressLineIndex() + 1;
                    StringBuffer outAddrStr = new StringBuffer();
                    for (int k = 0; k < addrCount; k++) {
                        outAddrStr.append(outAddr.getAddressLine(k));

                        latitude = outAddr.getLatitude();
                        longitude = outAddr.getLongitude();
                    }

                    startLocationUpdates();

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                }
            }
        }
        catch (IOException e) {
            Log.e(TAG, "Failed in using Geocoder", e);
        }
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        gMap.clear();
        googleMap = gMap;
        gMap.setOnMapClickListener(this);

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(latitude, longitude);
        if(sw == 1)
            gMap.addMarker(new MarkerOptions().position(location).title("Marker in now location"));
        else if(sw == 2)
            gMap.addMarker(new MarkerOptions().position(location).title("Marker in " + mAddressTextView.getText()));

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        gMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        // move the camera
        gMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }
}