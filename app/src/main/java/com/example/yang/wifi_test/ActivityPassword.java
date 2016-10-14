package com.example.yang.wifi_test;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivityPassword extends AppCompatActivity {

    Button cancelButton, connectButton;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        String SSID = getIntent().getStringExtra("SSID");
        setTitle("Please input password for " + SSID);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //getWindow().setLayout((int)(width * 1), (int)(height * 0.4));

        cancelButton = (Button) findViewById(R.id.cancel_button);
        connectButton = (Button) findViewById(R.id.connect_button);
        password = (EditText) findViewById(R.id.password);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("PASSWORD", password.getText().toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 8) {
                    connectButton.setEnabled(true);
                } else {
                    connectButton.setEnabled(false);
                }
            }
        });
    }
}
