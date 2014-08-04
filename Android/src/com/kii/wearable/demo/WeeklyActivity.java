package com.kii.wearable.demo;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.analytics.KiiAnalyticsException;
import com.kii.cloud.analytics.aggregationresult.DateRange;
import com.kii.cloud.analytics.aggregationresult.GroupedResult;
import com.kii.cloud.analytics.aggregationresult.GroupedSnapShot;
import com.kii.cloud.analytics.aggregationresult.ResultQuery;
import com.kii.cloud.analytics.aggregationresult.SimpleDate;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jjoe64.graphview.GraphView.GraphViewData;

/**
 * Created by tian on 14-6-24.
 */
public class WeeklyActivity extends BaseActivity {
    private LinearLayout contentView;
    private long startTime;
    private int itemCount;

    private Map<Long, Double> temperatureMap = new HashMap<Long, Double>();
    private Map<Long, Double> humidityMap = new HashMap<Long, Double>();
    private Map<Long, Double> uvMap = new HashMap<Long, Double>();
    private static final String TAG = WeeklyActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        contentView = (LinearLayout) findViewById(R.id.content);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getData() throws KiiAnalyticsException, JSONException {
        Calendar calendar = Calendar.getInstance();
        SimpleDate end = new SimpleDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        calendar.roll(Calendar.WEEK_OF_YEAR, -1);
        SimpleDate start = new SimpleDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        DateRange dateRange = new DateRange(start, end);
        ResultQuery query = ResultQuery.builderWithDateRange(dateRange).build();
        GroupedResult results = KiiAnalytics.getResult("120", query);
        List<GroupedSnapShot> snapshots = results.getSnapShots();
        if (snapshots.size() > 0) {
            GroupedSnapShot snapShot = snapshots.get(0);
            long pointStart = snapShot.getPointStart();
            startTime = pointStart / 1000;
            long interval = snapShot.getPointInterval() / 1000;
            android.util.Log.d(TAG, "temperature data is " + snapShot.getData());
            itemCount = snapShot.getData().length();
            for (int i = 0; i < itemCount; i++) {
                long index = startTime + interval * i;
                temperatureMap.put(index, snapShot.getData().getDouble(i));
            }
        }
        results = KiiAnalytics.getResult("106", query);
        snapshots = results.getSnapShots();
        if (snapshots.size() > 0) {
            GroupedSnapShot snapShot = snapshots.get(0);
            long pointStart = snapShot.getPointStart();
            startTime = pointStart / 1000;
            long interval = snapShot.getPointInterval() / 1000;
            android.util.Log.d(TAG, "humidity data is " + snapShot.getData());
            itemCount = snapShot.getData().length();
            for (int i = 0; i < itemCount; i++) {
                long index = startTime + interval * i;
                humidityMap.put(index, snapShot.getData().getDouble(i));
            }
        }
        results = KiiAnalytics.getResult("104", query);
        snapshots = results.getSnapShots();
        if (snapshots.size() > 0) {
            GroupedSnapShot snapShot = snapshots.get(0);
            long pointStart = snapShot.getPointStart();
            startTime = pointStart / 1000;
            long interval = snapShot.getPointInterval() / 1000;
            android.util.Log.d(TAG, "uv data is " + snapShot.getData());
            itemCount = snapShot.getData().length();
            for (int i = 0; i < itemCount; i++) {
                long index = startTime + interval * i;
                uvMap.put(index, snapShot.getData().getDouble(i));
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<GraphViewData> dataList = new ArrayList<GraphViewData>();
                GraphView graphView = new LineGraphView(
                        WeeklyActivity.this // context
                        , "Weekly Chart" // heading
                );
                for (long key : humidityMap.keySet()) {
                    GraphViewData vd = new GraphViewData(key, humidityMap.get(key));
                    dataList.add(vd);
                }
                GraphViewSeries.GraphViewSeriesStyle humidityStyle = new GraphViewSeries
                        .GraphViewSeriesStyle(getResources().getColor(android.R.color
                        .holo_red_light), 2);
                GraphViewSeries humiditySeries = new GraphViewSeries("Humidity", humidityStyle,
                        dataList.toArray(new GraphViewData[]{}));
                graphView.addSeries(humiditySeries);
                dataList.clear();
                for (long key : uvMap.keySet()) {
                    GraphViewData vd = new GraphViewData(key, uvMap.get(key));
                    dataList.add(vd);
                }
                GraphViewSeries.GraphViewSeriesStyle uvStyle = new GraphViewSeries
                        .GraphViewSeriesStyle(getResources().getColor(android.R.color
                        .holo_blue_bright), 2);
                GraphViewSeries uvSeries = new GraphViewSeries("UV", uvStyle,
                        dataList.toArray(new GraphViewData[]{}));
                graphView.addSeries(uvSeries);
                dataList.clear();
                for (long key : temperatureMap.keySet()) {
                    GraphViewData vd = new GraphViewData(key, temperatureMap.get(key));
                    dataList.add(vd);
                }
                GraphViewSeries.GraphViewSeriesStyle temperatureStyle = new GraphViewSeries
                        .GraphViewSeriesStyle(getResources().getColor(android.R.color
                        .holo_green_light), 2);
                GraphViewSeries temperatureSeries = new GraphViewSeries("Temperature",
                        temperatureStyle,
                        dataList.toArray(new GraphViewData[]{}));
                graphView.addSeries(temperatureSeries);
                graphView.setScalable(true);
                contentView.addView(graphView);
            }
        });
    }
}
