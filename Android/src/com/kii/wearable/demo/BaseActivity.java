package com.kii.wearable.demo;

import android.app.Activity;
import android.app.ProgressDialog;

/**
 * Created by tian on 14-6-24.
 */
public class BaseActivity extends Activity {
    protected ProgressDialog dialog = null;

    protected void showProgress(int resId) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }
        dialog.setTitle(resId);
        dialog.setIndeterminate(true);
        dialog.show();
    }

    protected void dismissProgress() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
