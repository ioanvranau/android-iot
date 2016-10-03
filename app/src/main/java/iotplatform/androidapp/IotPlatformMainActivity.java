package iotplatform.androidapp;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static iotplatform.androidapp.mqtt.MqttConnection.CLIENT_ID;
import static iotplatform.androidapp.mqtt.MqttConnection.PASSWORD;
import static iotplatform.androidapp.mqtt.MqttConnection.USER_NAME;
import static iotplatform.androidapp.mqtt.MqttConnection.getMqttClientConnection;
import static iotplatform.androidapp.mqtt.MqttConnection.publishMessage;

public class IotPlatformMainActivity extends AppCompatActivity {

    private static final String KEEP_TOURCH_ON_ON_CLOSE_CHECKBOX_STATE = "keepTourchOnOnCloseCheckBox";
    private Camera camera;

    private boolean isLighOn = false;
    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            savedInstanceState.getBoolean(KEEP_TOURCH_ON_ON_CLOSE_CHECKBOX_STATE);
        }
        setContentView(R.layout.activity_iot_platform_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (camera == null) {
            camera = Camera.open();
        }

        Button fab = (Button) findViewById(R.id.tourchSwitch);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    initMqttClient();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                TextView infoText = (TextView) findViewById(R.id.infoText);

                String message;
                if (isLighOn) {
                    message = "Torch is off!";
                    isLighOn = false;
                } else {
                    message = "Torch is on!";
                    isLighOn = true;
                }
                boolean cameraSwitched = cameraSwitch(!isLighOn);
                Log.i("info", message + "!");
                publishMessage(mqttClient, message);
                if (cameraSwitched) {
                    infoText.setText(message);
                } else {
                    String text = "Cammera could not be switched!";
                    infoText.setText(text);
                }
            }
        });

        Button acc = (Button) findViewById(R.id.accSensor);
        acc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IotPlatformMainActivity.this, AndroidAccelerometerExample.class);
                startActivity(intent);
            }
        });
    }

    private void initMqttClient() throws MqttException {
        mqttClient = getMqttClientConnection(CLIENT_ID);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(USER_NAME);
        connOpts.setPassword(PASSWORD.toCharArray());
        if (mqttClient != null && !mqttClient.isConnected()) {
            mqttClient.connect(connOpts);
        }
        initCallBack(mqttClient);
    }

    private boolean cameraSwitch(boolean isLighOn) {
        if (camera == null) {
            camera = Camera.open();
        }
        if (camera == null) {
            return false;
        }
        final Camera.Parameters p = camera.getParameters();
        if (isLighOn) {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            camera.stopPreview();
        } else {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            camera.startPreview();
        }

        return true;
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
        disconnectCameraAndMqqttClient();
    }

    private void disconnectCameraAndMqqttClient() {
        if (camera != null) {
            camera.release();
            camera = null;
            if (isLighOn) {
                isLighOn = false;
                String message = "Torch is off!";
                Log.i("info", message + "!");
                publishMessage(mqttClient, message);
            }
        }
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
