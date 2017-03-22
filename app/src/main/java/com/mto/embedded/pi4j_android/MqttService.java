package com.mto.embedded.pi4j_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Background service holding connection to MQTT broker and notify UI part of Android app on receiving MQTT message
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 3/22/17
 */

public class MqttService extends Service {

    private final static String TAG = "MqttService";

    /* package */ final static String MQTT_EXTRA = "com.mto.embedded.mqtt";

    /* package */ final static String MQTT_RESULT = "com.mto.embedded.mqtt_request_processed";

    private final static String MQTT_BROKER = "tcp://localhost:1883";

    private final static String MQTT_TOPIC = "mto/embedded/pi4j";

    private MqttAsyncClient asyncMqtt;

    private LocalBroadcastManager bcManager = LocalBroadcastManager.getInstance(this);

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            asyncMqtt = new MqttAsyncClient(MQTT_BROKER, MqttClient.generateClientId(), new MemoryPersistence());
            initializeMQTT();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to initialize MQTT", ex);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeMQTT() throws MqttException {
        asyncMqtt.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "Received MQTT message: " + message);
                try {
                    MotionSensorState msSt = new Gson().fromJson(new String(message.getPayload()), MotionSensorState.class);
                    String command = msSt.tripped ? "turn_camera_on" : "turn_camera_off";

                    Intent cmd = new Intent(MQTT_RESULT);
                    cmd.putExtra(MQTT_EXTRA, command);

                    bcManager.sendBroadcast(cmd);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to process arriving MQTT message ", ex);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttOptions = new MqttConnectOptions();
        mqttOptions.setCleanSession(true);
        asyncMqtt.connect(mqttOptions, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                try {
                    //Subscribe client to TOPIC
                    asyncMqtt.subscribe(MQTT_TOPIC, 2);
                } catch (MqttException mqttEx) {
                    Log.e("MqttService", "Failed to subscribe client to topic " + MQTT_TOPIC, mqttEx);
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

            }
        });

    }
}
