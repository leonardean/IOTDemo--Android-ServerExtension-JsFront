package com.kii.wearable.demo;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.storage.GeoPoint;
import com.kii.cloud.storage.KiiACL;
import com.kii.cloud.storage.KiiACLEntry;
import com.kii.cloud.storage.KiiAnyAuthenticatedUser;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiACLCallBack;
import com.kii.cloud.storage.callback.KiiObjectCallBack;

/**
 * Created by tian on 14-6-24.
 */
public class AddDataActivity extends BaseActivity implements
		View.OnClickListener {
	private EditText humidityEdit;
	private EditText uvEdit;
	private EditText temperatureEdit;
	private EditText deviceEdit;
	private TextView locationText;
	private static final String DELIMITER = ":";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_data);
		setTitle(R.string.add_new_data);
		initViews();
	}

	private LocationManager mLocationManager;
	private LocationListener mGpsListener;
	private LocationListener mNetworkListener;

	private Location mLastLocation;

	private void initViews() {
		humidityEdit = (EditText) findViewById(R.id.humidity_edit);
		uvEdit = (EditText) findViewById(R.id.uv_edit);
		temperatureEdit = (EditText) findViewById(R.id.temperature_edit);
		locationText = (TextView) findViewById(R.id.location_text);
		deviceEdit = (EditText) findViewById(R.id.device_id_edit);
		findViewById(R.id.save).setOnClickListener(this);
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mLastLocation = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (mLastLocation == null) {
			mLastLocation = mLocationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (mLastLocation != null) {
			locationText
					.setText(Double.toString(mLastLocation.getLatitude())
							+ DELIMITER
							+ Double.toString(mLastLocation.getLongitude()));
		}
		startLocationListeners();
	}

	private static final String TAG = AddDataActivity.class.getCanonicalName();

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.save:
			KiiBucket bucket = KiiUser.getCurrentUser().bucket("wdDataRecord");
			KiiACL ubACL = bucket.acl();
			ubACL.putACLEntry(new KiiACLEntry(KiiAnyAuthenticatedUser.create(),
					KiiACL.BucketAction.QUERY_OBJECTS_IN_BUCKET, true));
			ubACL.save(new KiiACLCallBack() {
				@Override
				public void onSaveCompleted(int token, KiiACL acl,
						Exception exception) {
					super.onSaveCompleted(token, acl, exception);
					if (exception != null) {
						Log.d(TAG, "onSaveACLComplete, exception is "
								+ exception.getMessage());
					}
				}
			});
			KiiObject data = bucket.object();
			try {
				double d = Double
						.parseDouble(humidityEdit.getText().toString());
				data.set("humidity", d);
				d = Double.parseDouble(temperatureEdit.getText().toString());
				data.set("temperature", d);
				d = Double.parseDouble(uvEdit.getText().toString());
				data.set("uv", d);
				data.set("deviceID", deviceEdit.getText().toString());
				GeoPoint point = new GeoPoint(mLastLocation.getLatitude(),
						mLastLocation.getLongitude());
				data.set("location", point);
				data.set("time", System.currentTimeMillis());
				KiiACL dataACL = data.acl();
				dataACL.putACLEntry(new KiiACLEntry(KiiAnyAuthenticatedUser
						.create(), KiiACL.ObjectAction.READ_EXISTING_OBJECT,
						true));
				dataACL.save(new KiiACLCallBack() {
					@Override
					public void onSaveCompleted(int token, KiiACL acl,
							Exception exception) {
						super.onSaveCompleted(token, acl, exception);
						if (exception != null) {
							Log.d(TAG, "onSaveACLComplete, exception is "
									+ exception.getMessage());
						}
					}
				});
				data.save(new KiiObjectCallBack() {
					@Override
					public void onSaveCompleted(int token, KiiObject object,
							Exception exception) {
						super.onSaveCompleted(token, object, exception);
						dismissProgress();
						if (exception == null) {
							Toast.makeText(AddDataActivity.this,
									R.string.save_complete, Toast.LENGTH_SHORT)
									.show();
							finish();
						} else {
							Toast.makeText(AddDataActivity.this,
									R.string.save_failed, Toast.LENGTH_SHORT)
									.show();
						}
					}
				});
				showProgress(R.string.saving);
			} catch (Exception e) {

			}
			break;
		}
	}

	private void startLocationListeners() {
		if (mLocationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			mNetworkListener = new LocationListenerImpl();
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 5, mNetworkListener);
		}
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mGpsListener = new LocationListenerImpl();
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 5, mGpsListener);
		}
	}

	private void stopLocationListeners() {
		if (null != mGpsListener) {
			mLocationManager.removeUpdates(mGpsListener);
			mGpsListener = null;
		}
		if (null != mNetworkListener) {
			mLocationManager.removeUpdates(mNetworkListener);
			mNetworkListener = null;
		}
	}

	private class LocationListenerImpl implements LocationListener {

		static final int TIME_THRESHOLD = 3 * 60 * 1000; // 3 minutes
		static final int DISTANCE_THRESHOLD = 150; // 150m

		public void onLocationChanged(Location location) {
			mLastLocation = location;
			refreshPlaces();

			if (Utils.newerThan(location.getTime(), TIME_THRESHOLD)
					&& location.getAccuracy() <= DISTANCE_THRESHOLD) {
				stopLocationListeners();
			}
		}

		public void onProviderDisabled(String provider) {
			// NO-OP
		}

		public void onProviderEnabled(String provider) {
			// NO-OP
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// NO-OP
		}

	}

	private void refreshPlaces() {
		locationText.setText(Double.toString(mLastLocation.getLatitude())
				+ DELIMITER + Double.toString(mLastLocation.getLongitude()));
	}

}
