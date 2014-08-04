package com.kii.wearable.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tian on 14-6-24.
 */
public class SelectDeviceActivity extends BaseActivity implements
		AdapterView.OnItemClickListener {
	private ListView listView;
	private List<String> objects = new ArrayList<String>();
	private KiiObjectAdapter adapter;
	private static final String TAG = SelectDeviceActivity.class
			.getCanonicalName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_device);
		setTitle(R.string.select_device);
		listView = (ListView) findViewById(R.id.list);
		adapter = new KiiObjectAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		// TODO query
		KiiClause clause = KiiClause.equals("userID", KiiUser.getCurrentUser()
				.toUri().getLastPathSegment());
		android.util.Log.d(TAG, "user id is "
				+ KiiUser.getCurrentUser().toUri().getLastPathSegment());
		KiiQuery query = new KiiQuery(clause);
		KiiBucket bucket = Kii.bucket("UserDeviceRelation");
		bucket.query(new KiiQueryCallBack<KiiObject>() {
			@Override
			public void onQueryCompleted(int token,
					KiiQueryResult<KiiObject> result, Exception exception) {
				super.onQueryCompleted(token, result, exception);
				dismissProgress();
				if (exception == null) {
					for (KiiObject object : result.getResult()) {
						if (object.keySet().contains("deviceID")) {
							Log.d(TAG, "device object is " + object.toJSON());
							String deviceId = object.getString("deviceID");
							if (objects.lastIndexOf(deviceId) < 0)
								objects.add(deviceId);
						}
					}
					adapter.notifyDataSetChanged();
				}
			}
		}, query);
		showProgress(R.string.getting_devices);

	}

	@Override
	public void onItemClick(AdapterView<?> view, View view2, int i, long l) {
		String deviceId = objects.get(i);
		Intent intent = new Intent(this, DailyChartActivity.class);
		intent.putExtra("device_id", deviceId);
		startActivity(intent);
	}

	private class KiiObjectAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return objects.size();
		}

		@Override
		public String getItem(int i) {
			return objects.get(i);
		}

		@Override
		public long getItemId(int i) {
			return 0;
		}

		@Override
		public View getView(int i, View view, ViewGroup group) {
			View v = view;
			if (view == null) {
				v = LayoutInflater.from(SelectDeviceActivity.this).inflate(
						android.R.layout.simple_list_item_1, group, false);
			}
			TextView tv = (TextView) v.findViewById(android.R.id.text1);
			tv.setText(objects.get(i));
			return v;
		}
	}
}
