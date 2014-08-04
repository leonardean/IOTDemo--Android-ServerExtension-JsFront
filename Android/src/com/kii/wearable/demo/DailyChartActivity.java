package com.kii.wearable.demo;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jjoe64.graphview.GraphView.GraphViewData;

/**
 * Created by tian on 14-6-24.
 */
public class DailyChartActivity extends BaseActivity {
    private static final String TAG = DailyChartActivity.class.getCanonicalName();
    private String deviceId;
    private LinearLayout contentView;
    private List<KiiObject> data = new ArrayList<KiiObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        contentView = (LinearLayout) findViewById(R.id.content);
        deviceId = getIntent().getStringExtra("device_id");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    queryData();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgress();
                        }
                    });
                }
            }
        }).start();
        showProgress(R.string.getting_daily_data);
    }

    private void queryData() throws ForbiddenException, BadRequestException, UndefinedException,
            IOException, ConflictException, NotFoundException, UnauthorizedException {
        KiiClause clause = KiiClause.equals("deviceID", deviceId);
        KiiBucket bucket = Kii.bucket("UserDeviceRelation");
        KiiQuery query = new KiiQuery(clause);
        KiiQueryResult<KiiObject> result = bucket.query(query);
        for (KiiObject object : result.getResult()) {
            KiiUser user;
            Log.d(TAG, "relation object is " + object.toJSON());
            if (object.getString("userID").contentEquals(KiiUser.getCurrentUser().toUri()
                    .getLastPathSegment())) {
                Log.d(TAG, "current user!");
                user = KiiUser.getCurrentUser();
            } else {
                continue;
            }
            KiiBucket dataBucket = user.bucket("wdDataRecord");
            KiiClause dataClause = KiiClause.equals("deviceID", deviceId);
            KiiQuery dataQuery = new KiiQuery(dataClause);
            KiiQueryResult<KiiObject> dataResult = dataBucket.query(dataQuery);
            for (KiiObject kiiObject : dataResult.getResult()) {
                android.util.Log.d(TAG, "object is " + kiiObject.toJSON());
                data.add(kiiObject);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissProgress();
                    //TODO: update data to the chart
                    List<GraphViewData> dataList = new ArrayList<GraphViewData>();
                    for (int i = 0; i < data.size(); i++) {
                        GraphViewData vd = new GraphViewData(i, data.get(i).getDouble("humidity"));
                        dataList.add(vd);
                    }
                    GraphView graphView = new LineGraphView(
                            DailyChartActivity.this // context
                            , "Daily Chart" // heading
                    );
                    GraphViewData[] dataArray = dataList.toArray(new GraphViewData[]{});
                    GraphViewSeries.GraphViewSeriesStyle humidityStyle = new GraphViewSeries
                            .GraphViewSeriesStyle(getResources().getColor(android.R.color
                            .holo_red_light), 2);
                    GraphViewSeries humiditySeries = new GraphViewSeries("Humidity", humidityStyle,
                            dataArray);
                    graphView.addSeries(humiditySeries);
                    dataList.clear();
                    for (int i = 0; i < data.size(); i++) {
                        GraphViewData vd = new GraphViewData(i, data.get(i).getDouble("uv"));
                        dataList.add(vd);
                    }
                    dataArray = dataList.toArray(new GraphViewData[]{});
                    GraphViewSeries.GraphViewSeriesStyle uvStyle = new GraphViewSeries
                            .GraphViewSeriesStyle(getResources().getColor(android.R.color
                            .holo_blue_bright), 2);
                    GraphViewSeries uvSeries = new GraphViewSeries("UV", uvStyle,
                            dataArray);
                    graphView.addSeries(uvSeries);
                    dataList.clear();
                    for (int i = 0; i < data.size(); i++) {
                        GraphViewData vd = new GraphViewData(i,
                                data.get(i).getDouble("temperature"));
                        dataList.add(vd);
                    }
                    dataArray = dataList.toArray(new GraphViewData[]{});
                    GraphViewSeries.GraphViewSeriesStyle temperatureStyle = new GraphViewSeries
                            .GraphViewSeriesStyle(getResources().getColor(android.R.color
                            .holo_green_light), 2);
                    GraphViewSeries temperatureSeries = new GraphViewSeries("Temperature",
                            temperatureStyle,
                            dataArray);
                    graphView.addSeries(temperatureSeries);
                    graphView.setScalable(true);
                    contentView.addView(graphView);
                }
            });
        }
    }
}
