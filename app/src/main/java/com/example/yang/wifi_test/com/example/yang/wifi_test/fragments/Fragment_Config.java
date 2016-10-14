package com.example.yang.wifi_test.com.example.yang.wifi_test.fragments;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yang.wifi_test.MainActivity;
import com.example.yang.wifi_test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yang on 10/11/2016.
 */

public class Fragment_Config extends Fragment {

    ListView list;
    List<WifiConfiguration> wifiConfiguration;
    WifiManager wifiManager;
    ConfigAdapter configAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.config_layout, container, false);
        list = (ListView) v.findViewById(R.id.listview1);

        wifiManager = (WifiManager)getActivity().getSystemService(MainActivity.WIFI_SERVICE);
        wifiConfiguration = wifiManager.getConfiguredNetworks();
        if (wifiConfiguration == null) {
            wifiConfiguration = new ArrayList<WifiConfiguration>();
        }

        configAdapter = new ConfigAdapter(getActivity());
        list.setAdapter(configAdapter);

        registerForContextMenu(list);

        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listview1) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(wifiConfiguration.get(info.position).SSID);
            menu.add(Menu.NONE, 0, 0, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        if (menuItemIndex == 0) {
            wifiManager.removeNetwork(wifiConfiguration.get(info.position).networkId);
            wifiManager.saveConfiguration();
            updateListView();
        }

        return true;
    }

    public void updateListView() {
        if (wifiManager != null) {
            List<WifiConfiguration> temp = wifiManager.getConfiguredNetworks();
            if (temp != null) {
                wifiConfiguration = temp;
            }
        }
        configAdapter.notifyDataSetChanged();
    }

    private class ConfigAdapter extends BaseAdapter {
        Context context;

        ConfigAdapter (Context c) {
            context = c;
        }

        @Override
        public int getCount() {
            return wifiConfiguration.size();
        }

        @Override
        public Object getItem(int position) {
            return wifiConfiguration.get(position);
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

            WifiConfiguration config = (WifiConfiguration) getItem(position);
            title.setText(config.SSID);
            desc.setText(getWifiConfigDesc(config));

            return row;
        }

        private String getWifiConfigDesc(WifiConfiguration config) {
            StringBuilder sbuf = new StringBuilder();

            sbuf.append("KeyMgmt:");
            for (int k = 0; k < config.allowedKeyManagement.size(); k++) {
                if (config.allowedKeyManagement.get(k)) {
                    sbuf.append(" ");
                    if (k < WifiConfiguration.KeyMgmt.strings.length) {
                        sbuf.append(WifiConfiguration.KeyMgmt.strings[k]);
                    } else {
                        sbuf.append("??");
                    }
                }
            }
            sbuf.append(" Protocols:");
            for (int p = 0; p < config.allowedProtocols.size(); p++) {
                if (config.allowedProtocols.get(p)) {
                    sbuf.append(" ");
                    if (p < WifiConfiguration.Protocol.strings.length) {
                        sbuf.append(WifiConfiguration.Protocol.strings[p]);
                    } else {
                        sbuf.append("??");
                    }
                }
            }

            return sbuf.toString();
        }
    }
}
