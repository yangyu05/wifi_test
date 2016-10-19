package com.example.yang.wifi_test;

import android.Manifest;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yang.wifi_test.com.example.yang.wifi_test.adapters.MyFragmentAdapter;
import com.example.yang.wifi_test.com.example.yang.wifi_test.fragments.Fragment_Config;
import com.example.yang.wifi_test.com.example.yang.wifi_test.fragments.Fragment_ScanResults;

import java.util.ArrayList;
import java.util.List;

import static android.widget.TabHost.*;

public class MainActivity extends AppCompatActivity implements OnPageChangeListener, OnTabChangeListener {

    private final static String TAG = "WifiTest";
    static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    Switch wifiSwitch;
    TextView connectInfo;
    TabHost tabhost;
    ViewPager viewpager;
    Handler refreshHandler;

    Fragment_ScanResults fragmentScanResults;
    Fragment_Config fragmentConfig;

    WifiManager wifiManager;
    WifiwifiReceiver wifiReceiver = new WifiwifiReceiver();

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            wifiManager.startScan();
            fragmentConfig.updateListView();

            refreshHandler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiSwitch = (Switch)findViewById(R.id.switch1);
        connectInfo = (TextView) findViewById(R.id.connect_info);

        wifiManager = (WifiManager)getSystemService(MainActivity.WIFI_SERVICE);

        initViewPager();

        initTabHost();

        refreshHandler = new Handler();
        refreshHandler.postDelayed(refreshRunnable, 2000);

        if (wifiManager.isWifiEnabled()) {
            wifiSwitch.setText(R.string.on);
            wifiSwitch.setChecked(true);
            getConnectedInfo();

            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CHANGE_WIFI_STATE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CHANGE_WIFI_STATE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                wifiManager.startScan();
            }
        } else {
            wifiSwitch.setText(R.string.off);
            wifiSwitch.setChecked(false);
        }

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (wifiManager.setWifiEnabled(true)) {
                        wifiSwitch.setText(R.string.on);
                        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.CHANGE_WIFI_STATE);
                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CHANGE_WIFI_STATE},
                                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    }
                } else {
                    if (wifiManager.setWifiEnabled(false)) {
                        wifiSwitch.setText(R.string.off);
                    }
                }
                fragmentScanResults.updateListView();
                fragmentConfig.updateListView();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        registerReceiver(wifiReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(wifiReceiver);
    }

    private void initViewPager() {
        viewpager = (ViewPager) findViewById(R.id.viewpager);

        List<Fragment> listFragements = new ArrayList<>();
        fragmentScanResults = new Fragment_ScanResults();
        fragmentConfig = new Fragment_Config();
        listFragements.add(fragmentScanResults);
        listFragements.add(fragmentConfig);

        viewpager.setAdapter(new MyFragmentAdapter(getSupportFragmentManager(), listFragements));
        viewpager.addOnPageChangeListener(this);
    }

    private void initTabHost() {
        tabhost = (TabHost) findViewById(android.R.id.tabhost);
        tabhost.setup();

        TabHost.TabSpec tabspec = tabhost.newTabSpec("tab1");
        tabspec.setIndicator("Netowrks");
        tabspec.setContent(new FakeContent(this));
        tabhost.addTab(tabspec);

        tabspec = tabhost.newTabSpec("tab2");
        tabspec.setIndicator("Configuration");
        tabspec.setContent(new FakeContent(this));
        tabhost.addTab(tabspec);

        tabhost.setOnTabChangedListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        tabhost.setCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onTabChanged(String tabId) {
        int selectedItem = tabhost.getCurrentTab();
        viewpager.setCurrentItem(selectedItem);
    }

    private class FakeContent implements TabContentFactory {

        Context context;

        public FakeContent(Context c) {
            context = c;
        }

        @Override
        public View createTabContent(String tag) {
            View fakeview = new View(context);
            fakeview.setMinimumHeight(0);
            fakeview.setMinimumWidth(0);
            return fakeview;
        }
    }

    public class WifiwifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                fragmentScanResults.updateListView();
                //wifiManager.startScan();
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Log.d(TAG, "Broadcast Recved = " + intent.getAction());
                if (WifiManager.WIFI_STATE_ENABLED == intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)) {
                    Toast.makeText(context, "Wifi enabled", Toast.LENGTH_SHORT).show();
                } else if (WifiManager.WIFI_STATE_DISABLED == intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)) {
                    Toast.makeText(context, "Wifi disabled", Toast.LENGTH_SHORT).show();
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Log.d(TAG, "Broadcast Recved = " + intent.getAction());
                WifiInfo info = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                if (info == null || info.getBSSID() == null) {
                    Log.d(TAG, "not connected");
                    connectInfo.setText(R.string.not_connected);
                } else {
                    Toast.makeText(context, "Wifi connected to " + info.getSSID(), Toast.LENGTH_SHORT).show();
                    getConnectedInfo();
                }
            } else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(intent.getAction())) {
                Log.d(TAG, "Broadcast Recved = " + intent.getAction());
                fragmentConfig.updateListView();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void getConnectedInfo()
    {
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info == null || info.getBSSID() == null) {
            connectInfo.setText(R.string.not_connected);
        } else {
            String infoStr = "Connected to " + " [SSID: " + info.getSSID() + "]" +
                    "[BSSID: " + info.getBSSID() + "]" +
                    "[IP: " + Formatter.formatIpAddress(info.getIpAddress()) + "]";
            connectInfo.setText(infoStr);
        }
    }
}
