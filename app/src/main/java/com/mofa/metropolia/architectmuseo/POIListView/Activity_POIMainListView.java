package com.mofa.metropolia.architectmuseo.POIListView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mofa.metropolia.architectmuseo.POINotification.Receiver_AlarmReceiver;
import com.mofa.metropolia.architectmuseo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class Activity_POIMainListView extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG_CATE = "Tag_Categories";

    private static final String TAG = "POIMainActivity";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    protected String locationStr;

    public static double mlat,mlong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poimain_list_view);
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        Toolbar toolbar = (Toolbar)findViewById(R.id.poi_list_toolbar);
        toolbar.setTitle("MFA");
        setSupportActionBar(toolbar);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity__poi, menu);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager)getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent();
                intent.putExtra(Activity_SearchResultActivity.Tag_SearchQuery, query);
                intent.putExtra(Activity_SearchResultActivity.TAG_LOCATION_LAT, mCurrentLocation.getLatitude());
                intent.putExtra(Activity_SearchResultActivity.TAG_LOCATION_LNG, mCurrentLocation.getLongitude());
                intent.setClass(getBaseContext(), Activity_SearchResultActivity.class);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };

        searchView.setOnQueryTextListener(listener);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.location_start) {
            scheduleAlarm();
            return true;
        }
        else if(id == R.id.location_stop){
            cancelAlarm();
            return true;
        }
        else if (id == R.id.action_refresh){
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void scheduleAlarm(){
        Log.e("MySer", "startAlarm");
        Intent intent = new Intent(getApplicationContext(), Receiver_AlarmReceiver.class);

        //create a pendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, Receiver_AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //alarm is set from now
        long firstMillis = System.currentTimeMillis();
        //setup periodic alarm every 10 seconds
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 5 * 1000, pIntent);

    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), Receiver_AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, Receiver_AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    @Override
    public void onConnected(Bundle bundle) {

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mCurrentLocation != null) {
            locationStr= mCurrentLocation.getLatitude()+"&lng="+mCurrentLocation.getLongitude();
            Log.e(TAG, locationStr);

            mlat = mCurrentLocation.getLatitude();
            mlong = mCurrentLocation.getLongitude();

            //Logic of tab view
            Adapter_MyViewPagerAdapter viewPagerAdapter = new Adapter_MyViewPagerAdapter(getSupportFragmentManager());
            viewPagerAdapter.setLocatStr(locationStr);

            if (getIntent().getStringExtra(TAG_CATE)!=null){
                viewPagerAdapter.setCateStr(getIntent().getStringExtra(TAG_CATE));
            }

            ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
            viewPager.setOffscreenPageLimit(viewPagerAdapter.getCount() - 1);
            viewPager.setAdapter(viewPagerAdapter);

            TabLayout tabLayout = (TabLayout)findViewById(R.id.fixed_tabs);
            tabLayout.setupWithViewPager(viewPager);
        }
        else {
            Toast.makeText(getBaseContext(), "No Location Service", Toast.LENGTH_SHORT).show();
            Intent settings = new Intent("com.google.android.gms.location.settings.GOOGLE_LOCATION_SETTINGS");
            startActivity(settings);
            finish();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        Log.e(TAG, result.toString());
        Toast.makeText(getBaseContext(),result.getErrorMessage(),Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

