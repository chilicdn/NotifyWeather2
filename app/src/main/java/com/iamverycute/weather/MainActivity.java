package com.iamverycute.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button btn;
    private Intent intent;
    private boolean isIgnore = false;

    @SuppressLint("BatteryLife")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btn = findViewById(R.id.btn_start);
        btn.setText(ForegroundService.isRunning ? R.string.btn_stop : R.string.btn_start);
        final PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isIgnore = manager.isIgnoringBatteryOptimizations(getPackageName());
        if (!isIgnore) {
            startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + getPackageName())));
        }
        intent = new Intent(this, ForegroundService.class);
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (isIgnore)
            finish();
        if (!ForegroundService.isRunning)
            System.exit(0);
    }

    public void ClickEvent(View view) {
        if (ForegroundService.isRunning) {
            stopService(intent);
            btn.setText(R.string.btn_start);
        } else {
            startForegroundService(intent);
            btn.setText(R.string.btn_stop);
        }
    }
}