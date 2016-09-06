package iotplatform.androidapp;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static iotplatform.androidapp.mqtt.MqttConnection.*;

public class IotPlatformMainActivity extends AppCompatActivity {

    private Camera camera;

    private boolean isLighOn = false;
    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_platform_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        camera = Camera.open();
        final Camera.Parameters p = camera.getParameters();

        Button fab = (Button) findViewById(R.id.initConnection);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mqttClient = getMqttClientConnection(CLIENT_ID);
                    MqttConnectOptions connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);
                    connOpts.setUserName(USER_NAME);
                    connOpts.setPassword(PASSWORD.toCharArray());
                    if(mqttClient != null && !mqttClient.isConnected()) {
                        mqttClient.connect(connOpts);
                    }
                    initCallBack(mqttClient);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                if (isLighOn) {

                    String message = "Torch is off!";
                    Log.i("info", message + "!");
                    String isLightOnBytes = Boolean.toString(!isLighOn);
                    publishMessage(mqttClient, message);
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                    camera.stopPreview();
                    isLighOn = false;


                } else {

                    Log.i("info", "torch is turn on!");
                    String message = "Torch is on!";
                    String isLightOnBytes = Boolean.toString(!isLighOn);
                    publishMessage(mqttClient, message);

                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

                    camera.setParameters(p);
                    camera.startPreview();
                    isLighOn = true;

                }

                String message = "No message received!";

            }
        });

        Button acc = (Button)findViewById(R.id.accSensor);
        acc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IotPlatformMainActivity.this, AndroidAccelerometerExample.class);
                startActivity(intent);
            }
        });
    }

    public void initCallBack(MqttClient mqttClient) {
        final MqttCallback callback = new MqttCallback() {
            public void messageArrived(String topic, MqttMessage msg)
                    throws Exception {
//                receivedMessage = msg;
//                receivedTopic = topic;
                System.out.println("Received from topic:" + topic);
                System.out.println("Received:" + new String(msg.getPayload()));
            }

            public void deliveryComplete(IMqttDeliveryToken arg0) {
                System.out.println("Delivery complete");
            }

            public void connectionLost(Throwable arg0) {
                arg0.printStackTrace();
                // TODO Auto-generated method stub
            }
        };
        mqttClient.setCallback(callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_iot_platform_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (camera != null) {
            camera.release();
        }
        if(mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
