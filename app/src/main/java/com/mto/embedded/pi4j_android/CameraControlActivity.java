package com.mto.embedded.pi4j_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class CameraControlActivity extends AppCompatActivity {

    protected BroadcastReceiver mqttReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_control);

        mqttReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(MqttService.MQTT_EXTRA);
                if("turn_on_camera".equals(s)){
                    turnOfCamera();
                }else if("turn_of_camera".equals(s)){
                    turnOnCamera();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mqttReceiver, new IntentFilter(MqttService.MQTT_RESULT));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mqttReceiver);
    }

    private void turnOnCamera(){
        Toast.makeText(this, "Receive turn_on_camera command", Toast.LENGTH_LONG).show();
    }

    private void turnOfCamera(){
        Toast.makeText(this, "Receive turn_off_camera command", Toast.LENGTH_LONG).show();
    }
}
