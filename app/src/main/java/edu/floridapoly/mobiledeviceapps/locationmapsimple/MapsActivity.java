package edu.floridapoly.mobiledeviceapps.locationmapsimple;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LatLng mCurrentLocation;
    private Double dblLat;
    private Double dblLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String strLat;
        String strLng;
        if(bundle != null){
            strLat = bundle.getString("LAT");
            strLng = bundle.getString("LNG");
            dblLat = Double.valueOf(strLat);
            dblLng = Double.valueOf(strLng);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateUI();
        /*
        // Add a marker in Sydney and move the camera
        LatLng currentLocation = new LatLng(29, -81);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker in Lat " + currentLocation.latitude + " - Lng " + currentLocation.longitude ));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        */

    }

    private void updateUI() {
        if (mMap == null ) {
            return;
        }

        //Location mCurrentLocation = new Location();
        LatLng mCurrentLocation = new LatLng(dblLat, dblLng);
        LatLng myPoint = new LatLng(
                mCurrentLocation.latitude, mCurrentLocation.longitude);
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        mMap.clear();
        mMap.addMarker(myMarker);

        LatLng mCurrentLocation2 = new LatLng(dblLat+1, dblLng+1);
        LatLng myPoint2 = new LatLng(
                mCurrentLocation2.latitude, mCurrentLocation2.longitude);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(myPoint)
                .include(myPoint2)
                .build();

        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);
    }

}
