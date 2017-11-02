package com.example.android.softkeyboard.processc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    private static final String BUTTON_TEXT = "BUTTON_TEXT";
    Button mButtonStartStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonStartStop = (Button) findViewById(R.id.buttonStartStop);
        mButtonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().equals("stop")) {
                    stopService(new Intent(MainActivity.this, MessengerUnicodeSenderService.class));
                    b.setText("start");
                } else {
                    startService(new Intent(MainActivity.this, MessengerUnicodeSenderService.class));
                    b.setText("stop");
                }
            }
        });
    }
}
