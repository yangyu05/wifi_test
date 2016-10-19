package com.example.yang.wifi_test.com.example.yang.wifi_test.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yang.wifi_test.ActivityPassword;
import com.example.yang.wifi_test.MainActivity;
import com.example.yang.wifi_test.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yang on 10/11/2016.
 */

public class Fragment_ScanResults extends Fragment {

    ListView list;
    List<ScanResult> scanResults = new ArrayList<>();
    WifiManager wifiManager = null;
    ScanResultsAdapter scanResultsAdapter;
    ScanResult to_connect;
    boolean viewCreated = false;

    static final int SECURITY_PROTO_MASK = 0xf;
    static final int SECURITY_OPEN = 0x0;
    static final int SECURITY_WEP = 0x1;
    static final int SECURITY_WPA = 0x2;
    static final int SECURITY_WPA2 = 0x4;

    static final int SECURITY_KEY_MNG_MASK = 0xf0;
    static final int SECURITY_PSK = 0x10;
    static final int SECURITY_EAP = 0x20;

    static final int SECURITY_CYPHER_MASK = 0xf00;
    static final int SECURITY_CCMP = 0x100;
    static final int SECURITY_TKIP = 0x200;

    public static final int PASSWORD_ACTIVITY = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.scan_result_layout, container, false);
        list = (ListView) v.findViewById(R.id.listview2);

        wifiManager = (WifiManager)getActivity().getSystemService(MainActivity.WIFI_SERVICE);
        scanResults = wifiManager.getScanResults();

        scanResultsAdapter = new ScanResultsAdapter(getActivity());
        list.setAdapter(scanResultsAdapter);

        list.setOnItemClickListener(new scanResultListener());

        viewCreated = true;
        return v;
    }

    public void updateListView() {
        if (viewCreated) {
            scanResults = wifiManager.getScanResults();
            Collections.sort(scanResults, new Comparator<ScanResult>() {
                public int compare(ScanResult r1, ScanResult r2) {
                    if (r1.level > r2.level) return -1;
                    if (r1.level < r2.level) return 1;
                    return 0;
                }
            });
            scanResultsAdapter.notifyDataSetChanged();
        }
    }

    private class ScanResultsAdapter extends BaseAdapter {
        Context context;

        ScanResultsAdapter (Context c) {
            context = c;
        }

        @Override
        public int getCount() {
            return scanResults.size();
        }

        @Override
        public Object getItem(int position) {
            return scanResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.single_row, parent, false);
            }

            ImageView image = (ImageView) row.findViewById(R.id.imageView);
            TextView title = (TextView) row.findViewById(R.id.textView3);
            TextView desc = (TextView) row.findViewById(R.id.textView4);

            ScanResult result = (ScanResult) getItem(position);
            title.setText(result.SSID);
            desc.setText(result.capabilities + "[RSSI: " + result.level + "dBm]");

            return row;
        }
    }

    private class scanResultListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            to_connect = scanResults.get(position);
            int to_connect_security = getSecurity(to_connect);
            if ((to_connect_security & SECURITY_EAP) != 0) {
                // get EAP credential
            } else if ((to_connect_security & SECURITY_PROTO_MASK) == SECURITY_OPEN) {
                // open security, do nothing
            } else {
                // WEP, WPA, or WPA2 security
                // check if network already in Configuration
                List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
                int networkId = -1;
                for (WifiConfiguration config : configs) {
                    String ssidStr = "\"" + to_connect.SSID + "\"";
                    if (ssidStr.equals(config.SSID)) {
                        networkId = config.networkId;
                        break;
                    }
                }

                if (networkId == -1) {
                    Intent intent = new Intent(getActivity(), ActivityPassword.class);
                    intent.putExtra("SSID", to_connect.SSID);
                    startActivityForResult(intent, PASSWORD_ACTIVITY);
                } else {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(networkId, true);
                    if (!wifiManager.reconnect()) {
                        Toast.makeText(getActivity(), "Failed to connect " + to_connect.SSID, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Connecting to " + to_connect.SSID, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PASSWORD_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                String password = data.getStringExtra("PASSWORD");

                WifiConfiguration config = new WifiConfiguration();
                config.SSID = "\"" + to_connect.SSID + "\"";
                config.preSharedKey = "\"" + password + "\"";

                int netId = wifiManager.addNetwork(config);
                wifiManager.saveConfiguration();
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                if (!wifiManager.reconnect()) {
                    Toast.makeText(getActivity(), "Failed to connect " + to_connect.SSID, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Connecting to " + to_connect.SSID, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    static int getSecurity(ScanResult result) {
        int security = SECURITY_OPEN;
        if (result.capabilities.contains("WEP")) {
            security |= SECURITY_WEP;
        }
        if (result.capabilities.contains("WPA-PSK")) {
            security |= SECURITY_WPA | SECURITY_PSK;
        }
        if (result.capabilities.contains("WPA2-PSK")) {
            security |= SECURITY_WPA2 | SECURITY_PSK;
        }
        if (result.capabilities.contains("WPA-EAP")) {
            security |= SECURITY_WPA | SECURITY_EAP;
        }
        if (result.capabilities.contains("WPA2-EAP")) {
            security |= SECURITY_WPA2 | SECURITY_EAP;
        }
        if (result.capabilities.contains("CCMP")) {
            security |= SECURITY_CCMP;
        }
        if (result.capabilities.contains("TKIP")) {
            security |= SECURITY_TKIP;
        }

        return security;
    }
}
