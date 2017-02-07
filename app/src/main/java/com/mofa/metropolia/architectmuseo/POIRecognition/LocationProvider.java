package com.mofa.metropolia.architectmuseo.POIRecognition;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;


public class LocationProvider implements LocationProviderInterface {

	private final LocationListener locationListener;
	private final LocationManager locationManager;
	private final int updateTime = 2000;
	private final int updateDistance = 0;
	private static final int timeOfOutdatedLocation	= 600000;//10min
	private boolean	gpsProviderEnabled, networkProviderEnabled;
	private final Context context;


	public LocationProvider(final Context context, LocationListener locationListener) {
		super();
		this.locationManager = (LocationManager)context.getSystemService( Context.LOCATION_SERVICE );
		this.locationListener = locationListener;
		this.context = context;
		this.gpsProviderEnabled = this.locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
		this.networkProviderEnabled = this.locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
	}

	@Override
	public void onPause() {
		if ( this.locationListener != null && this.locationManager != null && (this.gpsProviderEnabled || this.networkProviderEnabled) ) {
			this.locationManager.removeUpdates( this.locationListener );
		}
	}

	@Override
	public void onResume() {
		if ( this.locationManager != null && this.locationListener != null ) {
			this.gpsProviderEnabled = this.locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
			this.networkProviderEnabled = this.locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
			if ( this.gpsProviderEnabled ) {
				final Location lastKnownGPSLocation = this.locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
				if ( lastKnownGPSLocation != null && lastKnownGPSLocation.getTime() > System.currentTimeMillis() - timeOfOutdatedLocation ) {
					locationListener.onLocationChanged( lastKnownGPSLocation );
				}
				if (locationManager.getProvider(LocationManager.GPS_PROVIDER)!=null) {
					this.locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, updateTime, updateDistance, this.locationListener );
				}
			}
			if ( this.networkProviderEnabled ) {
				final Location lastKnownNWLocation = this.locationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
				if ( lastKnownNWLocation != null && lastKnownNWLocation.getTime() > System.currentTimeMillis() - timeOfOutdatedLocation ) {
					locationListener.onLocationChanged( lastKnownNWLocation );
				}
				if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER)!=null) {
					this.locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, updateTime, updateDistance, this.locationListener );
				}
			}
		}
	}
}
