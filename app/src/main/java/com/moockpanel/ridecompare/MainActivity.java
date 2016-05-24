package com.moockpanel.ridecompare;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import butterknife.InjectView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

//    permissions constant
    public static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 123;

    // tags for some debuggings
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String LOCALE = "LOCALE";
    private static final String LYFT_PACKAGE = "me.lyft.android";


    // Add Mixpanel project info
    String projectToken = "df39ed5a3dab58492d8e97362c727835";
    MixpanelAPI mMixpanel;
    MixpanelAPI.People mPeople;

    GoogleApiClient mGoogleApiClient;

    // uber and lyft member variables
    private RideDetails mLyft;
    private RideDetails mUber;

    // location member variables
    private Location mLastLocation;
    private double mLatitude;
    private double mLongitude;
    private String mStreetNumber;
    private String mStreetName;

    // butterknife UI elements
//    @InjectView(R.id.rideType) Switch mRideType;
//    @InjectView(R.id.destinationLabel) TextView mDestinationLabel;
//    @InjectView(R.id.destinationField) EditText mDestinationField;
//    @InjectView(R.id.pickupLabel) TextView mPickupLabel;
    @InjectView(R.id.pickupField) EditText mPickupField;
    //    @InjectView(R.id.lyftResults) TextView mLyftResults;
//    @InjectView(R.id.uberResults) EditText mUberResults;
    @InjectView(R.id.lyftRideType) TextView mLyftRideType;
    @InjectView(R.id.lyftAvgPrice) TextView mLyftAvgPrice;
    @InjectView(R.id.uberRideType) TextView mUberRideType;
    @InjectView(R.id.uberAvgPrice) TextView mUberAvgPrice;

    // lyft api credentials
    public static final String LYFT_API_KEY = "z2E761GAErN9";
    public static final String LYFT_API_SECRET = "54QePYI3fkABpTHdNtDKGl0e9Z8JWaTg";

    // sample api response for testing purposes
//    public static final String LYFT_API_RESPONSE = "{\"cost_estimates\": [{\"ride_type\": \"lyft_plus\", \"estimated_duration_seconds\": 812, \"estimated_distance_miles\": 3.29, \"estimated_cost_cents_max\": 1955, \"primetime_percentage\": \"0%\", \"currency\": \"USD\", \"estimated_cost_cents_min\": 1280, \"display_name\": \"RideDetails Plus\", \"primetime_confirmation_token\": null}, {\"ride_type\": \"lyft_line\", \"estimated_duration_seconds\": 812, \"estimated_distance_miles\": 3.29, \"estimated_cost_cents_max\": 524, \"primetime_percentage\": \"0%\", \"currency\": \"USD\", \"estimated_cost_cents_min\": 524, \"display_name\": \"RideDetails Line\", \"primetime_confirmation_token\": null}, {\"ride_type\": \"lyft\", \"estimated_duration_seconds\": 812, \"estimated_distance_miles\": 3.29, \"estimated_cost_cents_max\": 1355, \"primetime_percentage\": \"0%\", \"currency\": \"USD\", \"estimated_cost_cents_min\": 872, \"display_name\": \"RideDetails\", \"primetime_confirmation_token\": null}]";
    public static final String LYFT_API_RESPONSE = "{\"ride_type\": \"lyft_plus\", \"estimated_duration_seconds\": 812, \"estimated_distance_miles\": 3.29, \"estimated_cost_cents_max\": 1955, \"primetime_percentage\": \"0%\", \"currency\": \"USD\", \"estimated_cost_cents_min\": 1280, \"display_name\": \"RideDetails Plus\", \"primetime_confirmation_token\": null}, {\"ride_type\": \"lyft_line\", \"estimated_duration_seconds\": 812, \"estimated_distance_miles\": 3.29, \"estimated_cost_cents_max\": 524, \"primetime_percentage\": \"0%\", \"currency\": \"USD\", \"estimated_cost_cents_min\": 524, \"display_name\": \"RideDetails Line\", \"primetime_confirmation_token\": null}, {\"ride_type\": \"lyft\", \"estimated_duration_seconds\": 812, \"estimated_distance_miles\": 3.29, \"estimated_cost_cents_max\": 1355, \"primetime_percentage\": \"0%\", \"currency\": \"USD\", \"estimated_cost_cents_min\": 872, \"display_name\": \"RideDetails\", \"primetime_confirmation_token\": null}";
    public static final String UBER_API_RESPONSE = "{\"product_id\": \"26546650-e557-4a7b-86e7-6a3942445247\", \"currency_code\": \"USD\", \"display_name\": \"POOL\", \"estimate\": \"$7\", \"low_estimate\": 7, \"high_estimate\": 9, \"surge_multiplier\": 1.75, \"duration\": 640, \"distance\": 5.34}";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.inject(this);

        // initialize google api client
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

//        // initialize mixpanel
//        mMixpanel = MixpanelAPI.getInstance(this,projectToken);
//        mPeople = mMixpanel.getPeople();
//        String distinctId = mMixpanel.getDistinctId();
//        mMixpanel.identify(distinctId);
//        mPeople.identify(distinctId);
//        // mPeople.initPushHandling("fdsfdsafdsfdsfdsafsda");
//
//        // track app open
//        try {
//            JSONObject props = new JSONObject();
//            props.put("Fresh Launch", true);
//            mMixpanel.track("App Open", props);
//            mPeople.increment("App Opens",1);
//        } catch (JSONException e) {
//            Log.e("RIDESHARE", "Unable to add props");
//        }

        //        placeholder
//        getRideInfo("lyft", "x", 37.7772, -122.4233, 37.7972, -122.4533);


        try {
            mLyft = getRideDetails("lyft", LYFT_API_RESPONSE);
            mUber = getRideDetails("uber", UBER_API_RESPONSE);
//            updateDisplay();
        } catch (JSONException e) {
            Log.e("RIDESHARE", "Cant get RideDetails details");
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDisplay();
            }
        });
    }

    @OnClick(R.id.lyftButton)
    public void linkToLyft() {
        deepLinkIntoLyft();
    }

    @OnClick(R.id.uberButton)
    public void linkToUber() {
        deepLinkIntoUber();
    }

//    private void getRideInfo(String service, String type, double startLat, double startLng, double endLat, double endLng) {
//        String rideUrl;
//
////        select which service I will be calling
//        if (service.equals("uber")) {
//            rideUrl = "some uber shit in here";
//        }
//        else {
////            rideUrl = "--include -X GET -H 'Authorization: bearer " + getLyftToken() +
////                    "' / 'https://api.lyft.com/v1/cost?start_lat=" + startLat + "&start_lng="
////                    + startLng + "&end_lat=" + endLat + "&end_lng=" + endLng + "'";
//
//            rideUrl = "https://api.lyft.com/v1/cost?start_lat=" + startLat + "&start_lng="
//                    + startLng + "&end_lat=" + endLat + "&end_lng=" + endLng;
//        }
//
//        if (isNetworkAvailable()) {
//
////            build a consumer for oauth
//            OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(LYFT_API_KEY, LYFT_API_SECRET);
//            String lyftSecret = consumer.getConsumerSecret();
//            String lyftToken = consumer.getToken();
//            consumer.setTokenWithSecret(lyftToken, lyftSecret);
//
////            build out the client that will be sending a request
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .addInterceptor(new SigningInterceptor(consumer))
//                    .build();
//
////            build out the request with auth
//            Request request = new Request.Builder()
//                    .header("Authorization", lyftToken)
//                    .url(rideUrl)
//                    .build();
//            Log.v("REQUEST", request + "");
//
//            Call call = client.newCall(request);
//            call.enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    });
//                    alertUserAboutError();
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateDisplay();
//                        }
//                    });
//                    try {
//                        String jsonData = response.body().string();
//                        boolean requestStatus;
//                        Log.v(TAG, jsonData);
//                        if (response.isSuccessful()) {
//                            // parse my ride info then run the function to update the display
//                            mLyft = getRideDetails("lyft", LYFT_API_RESPONSE);
//                            mUber = getRideDetails("uber", UBER_API_RESPONSE);
//                            requestStatus = true;
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    updateDisplay();
//                                }
//                            });
//                        }
//                        else {
//                            alertUserAboutError();
//                            requestStatus = false;
//                        }
//                    }
//                    catch (IOException e) {
//                        Log.e(TAG, "Exception Caught", e);
//                    }
//                    catch (JSONException e) {
//                        Log.e(TAG, "Exception Caught", e);
//                    }
//                }
//            });
//        } else {
//            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
//        }
//
//    }

    // method for getting the necessary fields from the JSON object
    private RideDetails getRideDetails(String service, String jsonData) throws JSONException {

        JSONObject costEstimates = new JSONObject(jsonData);
        RideDetails rideDetails = new RideDetails();
        if (service.equals("lyft")) {
//            the json keys for lyft
            rideDetails.setRideType(costEstimates.getString("ride_type"));
            rideDetails.setCurrency(costEstimates.getString("currency"));
            rideDetails.setEstMaxCost(costEstimates.getInt("estimated_cost_cents_max"));
            rideDetails.setEstMinCost(costEstimates.getInt("estimated_cost_cents_min"));
            rideDetails.calcAvgCost(rideDetails.getEstMinCost(), rideDetails.getEstMaxCost());
            rideDetails.setPrimetimePercentage(costEstimates.getString("primetime_percentage"));
        }
//        the json keys for uber
        else {
            rideDetails.setRideType(costEstimates.getString("display_name"));
            rideDetails.setCurrency(costEstimates.getString("currency_code"));
            rideDetails.setEstMaxCost(costEstimates.getInt("low_estimate"));
            rideDetails.setEstMinCost(costEstimates.getInt("high_estimate"));
            rideDetails.calcAvgCost(rideDetails.getEstMinCost(), rideDetails.getEstMaxCost());
            rideDetails.setPrimetimePercentage(costEstimates.getString("surge_multiplier"));
        }
        return rideDetails;
    }

    // method for taking the data and updating ui elements
    private void updateDisplay () {
        mPickupField.setText(mStreetNumber + " " + mStreetName);
        mLyftAvgPrice.setText(String.valueOf(mLyft.getAvgCost()));
        mLyftRideType.setText(String.valueOf(mLyft.getRideType()));
        mUberAvgPrice.setText(String.valueOf(mUber.getAvgCost()));
        mUberRideType.setText(String.valueOf(mUber.getRideType()));
        Toast.makeText(this, mLatitude + "", Toast.LENGTH_LONG).show();
    }

    // detect connectivity status
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    // use this to get the address from my lat / long
    private void reverseGeocode(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<android.location.Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert addresses != null;
        if (addresses.size() > 0) {
            mStreetNumber = addresses.get(0).getSubThoroughfare();
            mStreetName = addresses.get(0).getThoroughfare();
        }
    }

    private void deepLinkIntoLyft() {
        if (isPackageInstalled(this, LYFT_PACKAGE)) {
            //This intent will help you to launch if the package is already installed
            openLink(this, "lyft://ridetype?id=" + mLyft.getRideType() + "&pickup[latitude]=" + mLatitude + "&pickup[longitude]=" + mLongitude + "&destination[latitude]=37.7763592&destination[longitude]=-122.4242038");

            Log.d(TAG, "Lyft is already installed on your phone, deeplinking.");
        } else {
            openLink(this, "https://play.google.com/store/apps/details?id=" + LYFT_PACKAGE);

            Log.d(TAG, "Lyft is not currently installed on your phone, opening Play Store.");
        }
    }

    static void openLink(Activity activity, String link) {
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
        playStoreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        playStoreIntent.setData(Uri.parse(link));
        activity.startActivity(playStoreIntent);
    }

    static boolean isPackageInstalled(Context context, String packageId) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageId, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            // ignored.
        }
        return false;
    }

    private void deepLinkIntoUber() {
        try {
            PackageManager pm = this.getPackageManager();
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            String uri =
                    "uber://?action=setPickup&pickup=my_location&client_id=YOUR_CLIENT_ID";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            // No Uber app! Open mobile website.
            String url = "https://m.uber.com/sign-up?client_id=YOUR_CLIENT_ID";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    private void checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
                return;
            }
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
            reverseGeocode(mLatitude, mLongitude);
        }
        updateDisplay();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorCode() + "", Toast.LENGTH_LONG).show();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}