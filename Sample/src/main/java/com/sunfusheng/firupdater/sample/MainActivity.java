package com.sunfusheng.firupdater.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sunfusheng.FirUpdater;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 10000;

    private DataUtil.AppConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recycler_view);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AppAdapter appAdapter = new AppAdapter(this, DataUtil.apps);
        recyclerView.setAdapter(appAdapter);

        appAdapter.setOnItemClickListener((adapter, holder, groupPosition, childPosition) -> {
            config = appAdapter.getItem(groupPosition, childPosition);
            if (config.key == 0) {
                return;
            }
            checkVersion();
        });
    }

    private void checkVersion() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new FirUpdater(this, config.appToken, config.appId).checkVersion();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && grantResults.length > 0) {
            new FirUpdater(this, config.appToken, config.appId).checkVersion();
        }
    }
}
