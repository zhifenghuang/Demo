package com.hzf.demo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.hzf.demo.R;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    private static final String[] APP_NEED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        } else {
            afterRequestPermission();
        }
    }


    public void requestPermission() {
        ArrayList<String> uncheckPermissions = null;
        for (String permission : APP_NEED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                if (uncheckPermissions == null) {
                    uncheckPermissions = new ArrayList<>();
                }
                uncheckPermissions.add(permission);
            }
        }
        if (uncheckPermissions != null && !uncheckPermissions.isEmpty()) {
            String[] array = new String[uncheckPermissions.size()];
            ActivityCompat.requestPermissions(this, uncheckPermissions.toArray(array), 101);
        } else {
            afterRequestPermission();
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                afterRequestPermission();
            }
        }
    }

    private void afterRequestPermission() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
}
