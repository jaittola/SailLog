package com.ja.saillog;

import android.app.Activity;
import android.widget.Toast;

public class SailLogActivityBase extends Activity {

    protected void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

}