package com.mofa.metropolia.architectmuseo.POIRecognition;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.mofa.metropolia.architectmuseo.POIListView.Activity_POIMainListView;
import com.mofa.metropolia.architectmuseo.POINotification.Receiver_AlarmReceiver;
import com.mofa.metropolia.architectmuseo.R;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectUrlListener;
import com.wikitude.architect.StartupConfiguration;

import java.io.IOException;

public abstract class AbstractArchitectCamActivity extends AppCompatActivity implements ArchitectViewHolderInterface {

	protected ArchitectView architectView;
	protected ArchitectUrlListener urlListener;
    protected LocationListener locationListener;
    protected Location lastKnownLocaton;
    protected LocationProviderInterface	locationProvider;
    protected ArchitectView.SensorAccuracyChangeListener sensorAccuracyListener;

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate( final Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
		/* pressing volume up/down should cause music volume changes */
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		this.setContentView(this.getContentViewId());
		this.setTitle(this.getActivityTitle());
		/* set AR-view for life-cycle notifications etc. */
		this.architectView = (ArchitectView)this.findViewById( this.getArchitectViewId()  );
		final StartupConfiguration config = new StartupConfiguration( this.getWikitudeSDKLicenseKey());

		try {
			/* first mandatory life-cycle notification */
			this.architectView.onCreate( config );
		} catch (RuntimeException rex) {
			this.architectView = null;
			Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
			Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
		}
        this.urlListener = this.getUrlListener();
		if (this.urlListener != null && this.architectView != null) {
			this.architectView.registerUrlListener( this.getUrlListener() );
		}


        this.sensorAccuracyListener = this.getSensorAccuracyListener();
        this.locationListener = new LocationListener() {

            @Override
            public void onStatusChanged( String provider, int status, Bundle extras ) {
            }

            @Override
            public void onProviderEnabled( String provider ) {
            }

            @Override
            public void onProviderDisabled( String provider ) {
            }

            @Override
            public void onLocationChanged( final Location location ) {
                if (location!=null) {
                    AbstractArchitectCamActivity.this.lastKnownLocaton = location;
                    if ( AbstractArchitectCamActivity.this.architectView != null ) {
                        if ( location.hasAltitude() && location.hasAccuracy() && location.getAccuracy()<7) {
                            AbstractArchitectCamActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy() );
                        } else {
                            AbstractArchitectCamActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
                        }
                    }
                }
            }
        };

        this.locationProvider = getLocationProvider( this.locationListener );
	}


	@Override
	protected void onPostCreate( final Bundle savedInstanceState ) {
		super.onPostCreate(savedInstanceState);
		if ( this.architectView != null ) {
			this.architectView.onPostCreate();

			try {
				this.architectView.load(this.getARchitectWorldPath());
                if(this.getARchitectWorldPath().equals("Cloud_Recognition/3dmodel.html")){
                    String title = getIntent().getStringExtra("title").replaceAll("\\s", "");
                    Double lat = getIntent().getDoubleExtra("lat", 0);
                    Double lng = getIntent().getDoubleExtra("lng", 0);
                    architectView.callJavascript("Recognition.createModel('" + title + "'," + lat + "," + lng + ")");
                }

				if (this.getInitialCullingDistanceMeters() != /*ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS*/1) {
					this.architectView.setCullingDistance( this.getInitialCullingDistanceMeters() );
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        if ( this.architectView != null ) {
            this.architectView.onResume();
            if (this.sensorAccuracyListener!=null) {
                this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
            }
        }
        if ( this.locationProvider != null ) {
            this.locationProvider.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ( this.architectView != null ) {
            this.architectView.onPause();
            if ( this.sensorAccuracyListener != null ) {
                this.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
            }
        }
        if ( this.locationProvider != null ) {
            this.locationProvider.onPause();
        }
    }

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if ( this.architectView != null ) {
			this.architectView.onDestroy();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if ( this.architectView != null ) {
			this.architectView.onLowMemory();
		}
	}

	public abstract String getActivityTitle();

	@Override
	public abstract String getARchitectWorldPath();

	@Override
	public abstract ArchitectUrlListener getUrlListener();

	@Override
	public abstract int getContentViewId();

	@Override
	public abstract String getWikitudeSDKLicenseKey();

	@Override
	public abstract int getArchitectViewId();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

    @Override
    public abstract LocationProviderInterface getLocationProvider(final LocationListener locationListener);

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.location_start) {
			scheduleAlarm();
			return true;
		}
		else if(id == R.id.location_stop){
			cancelAlarm();
			return true;
		}
		else if(id == R.id.show_list){
			Intent intent = new Intent();
			intent.setClass(this, Activity_POIMainListView.class);
			startActivity(intent);
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
		//setup periodic alarm every 5 seconds
		AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 5*1000, pIntent);

	}

	public void cancelAlarm() {
		Intent intent = new Intent(getApplicationContext(), Receiver_AlarmReceiver.class);
		final PendingIntent pIntent = PendingIntent.getBroadcast(this, Receiver_AlarmReceiver.REQUEST_CODE,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pIntent);
	}

}