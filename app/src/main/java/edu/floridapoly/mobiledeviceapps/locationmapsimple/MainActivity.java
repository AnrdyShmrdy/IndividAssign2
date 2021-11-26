package edu.floridapoly.mobiledeviceapps.locationmapsimple;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    StringBuffer response;
    String responseText;
    String lon;
    String lat;
    String temp;
    String desc;
    TextView lonTxtView;
    TextView latTxtView;
    TextView tempTxtView;
    TextView descTxtView;
    private static final String TAG = "LocationMapSimple";
    private static final int REQUEST_ERROR = 0;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;

    private GoogleApiClient mClient;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lonTxtView = findViewById(R.id.editTxt_lng);
        latTxtView = findViewById(R.id.editTxt_lat);
        tempTxtView = findViewById(R.id.tempVal);
        descTxtView = findViewById(R.id.descValue);
        mClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        // do something
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = apiAvailability.getErrorDialog(this,
                    errorCode,
                    REQUEST_ERROR,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            // Leave if services are unavailable.
                            finish();
                        }
                    });

            errorDialog.show();
        }
    }
    public void GetDataFRomWebService(View view) {
        TextView cityText;
        cityText = findViewById(R.id.cityValue);
        String city = cityText.getText().toString();
        String urlStr = "https://api.openweathermap.org/data/2.5/weather?appid=ec213c940f03a1c6345e21f4a0985758";
        urlStr = urlStr + "&" + "units=imperial";
        urlStr = urlStr + "&" + "q=";
        String url = urlStr + city;
        Log.i("URL:", url);
        new BackgroundWebAccess().execute(url);
    }
    class BackgroundWebAccess extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            return AccessInternet(objects[0].toString());
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            //textViewReceivedData.setText(responseText);
            tempTxtView.setText(temp);
            lonTxtView.setText(lon);
            latTxtView.setText(lat);
            descTxtView.setText(desc);


        }

        protected Void AccessInternet(String urlStr) {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    // Reading response from input Stream
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()));
                    String output;
                    response = new StringBuffer();
                    while ((output = in.readLine()) != null) {
                        response.append(output);
                    }
                    in.close();
                }

                responseText = response.toString();
                Log.i("WebService", responseText);

                JSONObject jsonResponse = new JSONObject(responseText);
                JSONObject coordinates = jsonResponse.getJSONObject("coord");
                JSONObject weather = jsonResponse.getJSONArray("weather").getJSONObject(0);
                JSONObject main = jsonResponse.getJSONObject("main");
                lon = coordinates.getString("lon");
                lat = coordinates.getString("lat");
                temp = main.getString("temp");
                desc = weather.getString("description");
            } catch (Exception ex) {
                //do something
            }

            return null;
        }

    }
    public void clickedGetLocation(View view) {
        if (hasLocationPermission()) {
            getLocation();
        } else {
            requestPermissions(LOCATION_PERMISSIONS,
                    REQUEST_LOCATION_PERMISSIONS);
        }
    }

    public void clickedGo2Map(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        EditText editTextLat = findViewById(R.id.editTxt_lat);
        String strLat = editTextLat.getText().toString();
        EditText editTextLng = findViewById(R.id.editTxt_lng);
        String strLng = editTextLng.getText().toString();

        intent.putExtra("LAT", strLat);
        intent.putExtra("LNG", strLng);

        startActivity(intent);
    }

    private boolean hasLocationPermission() {
        int result = ContextCompat
                .checkSelfPermission(this, LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS:
                if (hasLocationPermission()) {
                    getLocation();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getLocation() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);


        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(request);
        LocationSettingsRequest locationSettingsRequest = builder.build();


        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(request, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        TextView textView = findViewById(R.id.txt_location_info);
                        textView.setText("Lat: " + locationResult.getLastLocation().getLatitude() + "\n"
                                + "Lng: " + locationResult.getLastLocation().getLongitude());
                    }
                },
                Looper.myLooper());

        fusedLocationClient = getFusedLocationProviderClient(getBaseContext());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            Log.i(TAG, "Got a fix: " + location);
                            Toast toast = Toast.makeText(getBaseContext(),"Got a fix: " + location,Toast.LENGTH_SHORT);
                            toast.show();

                            TextView textView = findViewById(R.id.txt_location_info);
                            textView.setText("Lat: " + location.getLatitude() + "\n" + "Lng: " + location.getLongitude() );

                            EditText editTextLat = findViewById(R.id.editTxt_lat);
                            editTextLat.setText(String.valueOf(location.getLatitude()));
                            EditText editTextLng = findViewById(R.id.editTxt_lng);
                            editTextLng.setText(String.valueOf(location.getLongitude()));

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });

    }


    @Override
    public void onStop() {
        super.onStop();

        mClient.disconnect();
    }

}
