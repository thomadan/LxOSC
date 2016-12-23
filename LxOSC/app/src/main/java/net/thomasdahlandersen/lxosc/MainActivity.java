
// This is the main activity and the only activity of this application,
// sending light sensor values as
// The JavaOSC library originally developed by illposed and maintained by    : com.illposed.osc:javaosc-core:0.3

package net.thomasdahlandersen.lxosc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Arrays;

import com.illposed.osc.*;



// Implementing light sensor listener and spinner's on item selected listener
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final String PREFS_NAME = "MyPrefsFile";

    private String myIP;
    private int myPort;
    private OSCPortOut oscPortOut;                                  // JavaOSC type IP and port

    TextView valueDisplay;
    EditText editTextIP;
    EditText editTextPort;

    private SensorManager mSensorManager;                           // Initialize sensor
    private Sensor mSensor;
    double sensorValue = 0;
    double outSensorValue = 0;
    double maxValue = 1000;
    DecimalFormat numberFormat = new DecimalFormat("0");

    double lastSensorValue = 0;


    private ProgressBar progressBar;



    /****************************************************************
     * Standard overrides
     *****************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        valueDisplay = (TextView) findViewById(R.id.valueDisplay);
        editTextIP = (EditText) findViewById(R.id.ip);
        editTextPort = (EditText) findViewById(R.id.port);

        editTextIP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    myIP = editTextIP.getText().toString();


                    // Register new IP
                    try {
                        oscPortOut = new OSCPortOut(InetAddress.getByName(myIP), myPort);// Connect to some IP address and port
                    } catch (UnknownHostException e) {
                        Log.v("IP", "No IP?"); // Error handling when your IP isn't found
                    } catch (Exception e) {
                        Log.v("Network error", "Network error");// Error handling for any other errors
                    }


                    editTextIP.clearFocus();

                    InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    handled = true;
                }
                return handled;
            }
        });

        editTextPort.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    myPort = Integer.parseInt(editTextPort.getText().toString());

                    // Register new port
                    try {
                        oscPortOut = new OSCPortOut(InetAddress.getByName(myIP), myPort);// Connect to some IP address and port
                    } catch (UnknownHostException e) {
                        Log.v("IP", "No IP?"); // Error handling when your IP isn't found
                    } catch (Exception e) {
                        Log.v("Network error", "Network error");// Error handling for any other errors
                    }

                    editTextPort.clearFocus();

                    InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    handled = true;
                }
                return handled;
            }
        });


        /** Restore preferences */
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        myIP = settings.getString("ipAddress", "DEFAULT");
        editTextIP.setText(myIP);

        myPort = settings.getInt("port2", 0);
        editTextPort.setText(Integer.toString(myPort));



        /** Initialize sensor */
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);


        /** Start the thread that sends messages over UDP */
        oscThread.start();


        /** Progress bar */
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        // Set max value according to reported maximum range of device sensor
        maxValue = mSensor.getMaximumRange();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);                                    // Make sure to unregister sensor listener on pause, otherwise it will continue to use resources
    }


    @Override
    protected void onStop(){
        super.onStop();

        /** Save IP and port to preferences */
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);           // Get location of preferences file
        SharedPreferences.Editor editor = settings.edit();                          // Create an Editor object to make preference changes.
        editor.putString("ipAddress", myIP);                                        // Send IP to editor
        editor.putInt("port2", myPort);                                             // Send port number to editor
        editor.commit();                                                            // Commit the edits to preferences file
    }


    @Override
    public void onBackPressed() {
    }


    // This entire override added from
    // http://stackoverflow.com/questions/4828636/edittext-clear-focus-on-touch-outside
    // in order to lose focus on touch outside edittext
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }


    /**************************************************
    Required overrides of sensor methods onSensorChanged and onAccuracyChanged.
    **************************************************/
    @Override
    public void onSensorChanged(SensorEvent event) {                                // If the light level changes

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {                          //
            sensorValue = event.values[0];

            if (sensorValue > maxValue) {                                           // Limit to a maximum value set by user
                sensorValue = maxValue;
            }

            outSensorValue = lastSensorValue + ((sensorValue - lastSensorValue) * 0.04);

            progressBar.setProgress((int)sensorValue * 100);                              // Set state of graphical representation
            valueDisplay.setText("" + numberFormat.format(outSensorValue));

            lastSensorValue = outSensorValue;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }



    /*********************************************************
     * Thread for sending over network because Android does not allow networking in main thread
     * This code is mostly from illposed example
     *********************************************************/
    private Thread oscThread = new Thread() {
        @Override
        public void run() {

            try {
                oscPortOut = new OSCPortOut(InetAddress.getByName(myIP), myPort);// Connect to some IP address and port
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
                    OSCMessage message = new OSCMessage("/lxosc/lx/", Arrays.asList(thingsToSend));

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
