package com.kii.wearable.demo;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tian on 14-6-24.
 */
public class MyDataListActivity extends BaseActivity {
    private ListView listView;
    private List<KiiObject> objects = new ArrayList<KiiObject>();
    private KiiObjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.all_my_data);
        setContentView(R.layout.activity_all_data);
        listView = (ListView) findViewById(R.id.list);
        listView = (ListView) findViewById(R.id.list);
        adapter = new KiiObjectAdapter();
        listView.setAdapter(adapter);
        KiiQuery all_query = new KiiQuery();
        KiiBucket bucket = KiiUser.getCurrentUser().bucket("wdDataRecord");
        bucket.query(new KiiQueryCallBack<KiiObject>() {
            @Override
            public void onQueryCompleted(int token, KiiQueryResult<KiiObject> result,
                                         Exception exception) {
                dismissProgress();
                objects.addAll(result.getResult());
                adapter.notifyDataSetChanged();
            }
        }, all_query);
        showProgress(R.string.getting_my_data);
    }

    private static final String DATE_FORMAT_STRING = "yy-MM-dd hh:mm:ss";

    private class KiiObjectAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public KiiObject getItem(int i) {
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
                v = LayoutInflater.from(MyDataListActivity.this).inflate(android.R.layout
                        .simple_list_item_2, group, false);
            }
            KiiObject object = objects.get(i);
            TextView tv1 = (TextView) v.findViewById(android.R.id.text1);
            tv1.setText(object.getString("deviceID") +" : "+ DateFormat.format(DATE_FORMAT_STRING,
                    new Date(object.getLong("_created"))));
            TextView tv2 = (TextView) v.findViewById(android.R.id.text2);
            tv2.setText(String.format("temperature:%.2f humidity:%.2f uv:%.2f",
                    object.getDouble("temperature"), object.getDouble("humidity"),
                    object.getDouble("uv")));
            return v;
        }
    }
}
