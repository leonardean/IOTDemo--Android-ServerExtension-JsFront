package com.kii.wearable.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.add_new_data).setOnClickListener(this);
        findViewById(R.id.daily_chart).setOnClickListener(this);
        findViewById(R.id.log_out).setOnClickListener(this);
        findViewById(R.id.monthly_chart).setOnClickListener(this);
        findViewById(R.id.weekly_chart).setOnClickListener(this);
        findViewById(R.id.show_all_my_data).setOnClickListener(this);
        if (!Settings.isLoggedIn(this)) {
            startActivity(new Intent(this, LogInActivity.class));
        } else {
            //TODO: log in in background?
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        KiiUser.loginWithToken(new KiiUserCallBack() {
                            @Override
                            public void onLoginCompleted(int token, KiiUser user,
                                                         Exception exception) {
                                dismissProgress();
                                if (exception != null) {
                                    startActivity(new Intent(MainActivity.this,
                                            LogInActivity.class));
                                } else {
                                    Settings.setUserToken(getApplicationContext(),
                                            user.getAccessToken());
                                    Settings.setUserId(getApplicationContext(), user.toUri()
                                            .getLastPathSegment());
                                }
                            }
                        }, Settings.getUserToken(MainActivity.this));
                    } catch (Exception e) {

                    }
                }
            }).start();
            showProgress(R.string.logging_in);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.add_new_data:
                intent = new Intent(this, AddDataActivity.class);
                startActivity(intent);
                break;
            case R.id.daily_chart:
                intent = new Intent(this, SelectDeviceActivity.class);
                startActivity(intent);
                break;
            case R.id.weekly_chart:
                intent = new Intent(this, WeeklyActivity.class);
                startActivity(intent);
                break;
            case R.id.monthly_chart:
                break;
            case R.id.show_all_my_data:
                intent = new Intent(this, MyDataListActivity.class);
                startActivity(intent);
                break;
            case R.id.log_out:
                Settings.setUserId(this, "");
                Settings.setUserToken(this, "");
                Toast.makeText(this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LogInActivity.class));
                break;
        }
    }

}
