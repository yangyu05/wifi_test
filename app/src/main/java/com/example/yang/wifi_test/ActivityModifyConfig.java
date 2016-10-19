package com.example.yang.wifi_test;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class ActivityModifyConfig extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner spinner;
    EditText password;
    Button buttonSave, buttonCancel;

    WifiConfiguration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_config);

        config = getIntent().getParcelableExtra("CONFIG");
        String SSID = config.SSID.substring(1, config.SSID.length() - 2);
        setTitle("Please modify settings for " + SSID);

        buttonSave = (Button) findViewById(R.id.button_save);
        buttonCancel = (Button) findViewById(R.id.button_cancel);
        password = (EditText) findViewById(R.id.editText);
        password.setText("");

        spinner = (Spinner) findViewById(R.id.spinner2);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.security_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
            if (config.wepKeys[0].length() == 0) {
                // OPEN
                spinner.setSelection(0);
            } else {
                // WEP
                spinner.setSelection(1);
            }
        } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            if (config.allowedProtocols.get(WifiConfiguration.Protocol.WPA) &&
                    config.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
                // wpa/wpa2-psk
                spinner.setSelection(4);
            } else if (config.allowedProtocols.get(WifiConfiguration.Protocol.WPA)) {
                // wpa-psk
                spinner.setSelection(2);
            } else if (config.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
                // wpa2-psk
                spinner.setSelection(3);
            } else {
                spinner.setSelection(6);
            }
        } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            // wpa/wpa2-eap
            spinner.setSelection(5);
        } else {
            spinner.setSelection(6);
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wifiManager = (WifiManager) getSystemService(MainActivity.WIFI_SERVICE);
                int sel = spinner.getSelectedItemPosition();
                switch (sel) {
                    case 0:
                        config.allowedKeyManagement.clear();
                        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        config.wepKeys[0] = "";
                        break;
                    case 1:
                        config.allowedKeyManagement.clear();
                        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        config.wepKeys[0] = "\"" + password.getText().toString() + "\"";
                        break;
                    case 2:
                        config.allowedKeyManagement.clear();
                        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        config.allowedProtocols.clear();
                        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        config.preSharedKey = "\"" + password.getText().toString() + "\"";
                        break;
                    case 3:
                        config.allowedKeyManagement.clear();
                        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        config.allowedProtocols.clear();
                        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        config.preSharedKey = "\"" + password.getText().toString() + "\"";
                        break;
                    case 4:
                        config.allowedKeyManagement.clear();
                        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        config.allowedProtocols.clear();
                        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        config.preSharedKey = "\"" + password.getText().toString() + "\"";
                        break;
                    case 5:
                        config.allowedKeyManagement.clear();
                        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                        config.allowedProtocols.clear();
                        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        config.preSharedKey = "\"" + password.getText().toString() + "\"";
                        break;
                    case 6:
                    default:
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityModifyConfig.this);
                        builder.setMessage("Must set a valid security mode")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });
                        // Create the AlertDialog object and return it
                        builder.create().show();
                        return;
                }
                wifiManager.updateNetwork(config);
                wifiManager.saveConfiguration();
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            }
        });

        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            password.setEnabled(false);
        } else {
            password.setEnabled(true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
