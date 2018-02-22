package com.sunfusheng.firupdater.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sunfusheng.FirUpdater;
import com.sunfusheng.FirUpdaterUtils;

public class MainActivity extends AppCompatActivity {

    private DataSource.AppConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recycler_view);

        setTitle(getString(R.string.app_name) + "（V" + FirUpdaterUtils.getVersionName(this) + "）");

        new FirUpdater(this, DataSource.API_TOKEN, DataSource.FIR_UPDATER_APP_ID).checkVersion();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AppsAdapter appsAdapter = new AppsAdapter(this, DataSource.apps);
        recyclerView.setAdapter(appsAdapter);

        appsAdapter.setOnItemClickListener((adapter, holder, groupPosition, childPosition) -> {
            config = appsAdapter.getItem(groupPosition, childPosition);
            if (config.key == 0) {
                return;
            }

            if (FirUpdaterUtils.isAppInstalled(this, getString(config.pkgId))) {
                FirUpdaterUtils.startApp(this, getString(config.pkgId));
                return;
            }

            new FirUpdater(this, DataSource.API_TOKEN, getString(config.appId))
                    .enableForceShowDialog(true)
                    .checkVersion();
        });
    }

}
