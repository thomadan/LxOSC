

// This is the main activity and the only activity of this application,
// sending light sensor values as

// The JavaOSC library originally developed by illposed and maintained by    : com.illposed.osc:javaosc-core:0.3


package net.thomasdahlandersen.osclight;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.illposed.osc.*;


public class MainActivity extends AppCompatActivity implements SensorEventListener  {

    // These are to be set in config util todo
    private String myIP = "192.168.10.110";
    private int myPort = 8000;

    private OSCPortOut oscPortOut;

    TextView valueDisplay;
    TextView maxDisplay;

    // Initialize sensor
    private SensorManager mSensorManager;
    private Sensor mSensor;

    double sensorValue = 0;

    int maxValue = 1000;

    // Seek bar
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        valueDisplay = (TextView) findViewById(R.id.valueDisplay);
        maxDisplay = (TextView) findViewById(R.id.maxDisplay);

        // sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Start the thread that sends messages
        oscThread.start();

        seekBar = (SeekBar) findViewById(R.id.seekBar2);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                maxValue = progress;
                maxDisplay.setText("" + maxValue);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }


    // SensorEventListener demands use of two methods, onSensorChanged and onAccuracyChanged.
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            sensorValue = event.values[0];

            if (sensorValue > maxValue) {
                sensorValue = maxValue;
            }

            valueDisplay.setText("lx: " + sensorValue);
        }
    }


    // This is one of the required super implementations.
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    // Thread for sending over network because Android does not allow network outside thread
    private Thread oscThread = new Thread() {
        @Override
        public void run() {

            try {
                // Connect to some IP address and port
                oscPortOut = new OSCPortOut(InetAddress.getByName(myIP), myPort);
            } catch (UnknownHostException e) {
                Log.v("IP", "No IP?"); // Error handling when your IP isn't found
                return;
            } catch (Exception e) {
                Log.v("Network error", "Network error");// Error handling for any other errors
                return;
            }

            // Send message
            while (true) {
                if (oscPortOut != null) {
                    // Creating the message
                    Object[] thingsToSend = new Object[1];
                    thingsToSend[0] = Double.toString(sensorValue);
                    OSCMessage message = new OSCMessage("/wec/inputs", Arrays.asList(thingsToSend));

                    // Send message
                    try {
                        oscPortOut.send(message);
                        sleep(25);
                    } catch (Exception e) {
                    }
                }
            }
        }
    };
}
