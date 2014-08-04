package com.kii.wearable.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;

/**
 * Created by tian on 14-6-24.
 */
public class LogInActivity extends BaseActivity implements View.OnClickListener {
    EditText usernameEdit;
    EditText passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        callback = new UserCallback();
        setTitle(R.string.kii_log_in);
        usernameEdit = (EditText) findViewById(R.id.username_edit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        findViewById(R.id.btn_register).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                KiiUser.logIn(callback, usernameEdit.getText().toString(),
                        passwordEdit.getText().toString());
                showProgress(R.string.logging_in);
                break;
            case R.id.btn_register:
                KiiUser user = KiiUser.builderWithName(usernameEdit.getText().toString()).build();
                user.register(callback, passwordEdit.getText().toString());
                showProgress(R.string.registering);
                break;
        }
    }

    private UserCallback callback;

    public class UserCallback extends KiiUserCallBack {
        @Override
        public void onLoginCompleted(int token, KiiUser user, Exception exception) {
            dismissProgress();
            if (exception == null) {
                Settings.setUserToken(getApplicationContext(),
                        user.getAccessToken());
                Settings.setUserId(getApplicationContext(), user.toUri()
                        .getLastPathSegment());
                finish();
            } else {
                Toast.makeText(LogInActivity.this, R.string.log_in_failed,
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onRegisterCompleted(int token, KiiUser user, Exception exception) {
            dismissProgress();
            if (exception == null) {
                Settings.setUserToken(getApplicationContext(),
                        user.getAccessToken());
                Settings.setUserId(getApplicationContext(), user.toUri()
                        .getLastPathSegment());
                finish();
            } else {
                Toast.makeText(LogInActivity.this, R.string.register_failed,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
