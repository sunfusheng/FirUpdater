package com.sunfusheng.firupdater.sample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sunfusheng.FirUpdater;
import com.sunfusheng.UpdaterUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recycler_view);

        setTitle(getString(R.string.app_name) + "（V" + UpdaterUtil.getVersionName(this) + "）");

        FirUpdater.getInstance(this)
                .apiToken(DataSource.API_TOKEN)
                .appId(DataSource.FIR_UPDATER_APP_ID)
                .packageName(UpdaterUtil.getPackageName(this))
                .checkVersion();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AppsAdapter appsAdapter = new AppsAdapter(this, DataSource.apps);
        recyclerView.setAdapter(appsAdapter);

        appsAdapter.setOnItemClickListener((adapter, holder, groupPosition, childPosition) -> {
            DataSource.AppConfig config = appsAdapter.getItem(groupPosition, childPosition);
            if (config.key == 0) {
                return;
            }

            if (UpdaterUtil.isAppInstalled(this, getString(config.pkgId))) {
                UpdaterUtil.startApp(this, getString(config.pkgId));
                return;
            }

            FirUpdater.getInstance(this)
                    .apiToken(DataSource.API_TOKEN)
                    .appId(getString(config.appId))
                    .packageName(getString(config.pkgId))
                    .checkVersion();
        });


    }
}
