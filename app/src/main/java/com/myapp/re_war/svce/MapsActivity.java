package com.myapp.re_war.svce;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import java.io.IOException;
import java.util.UUID;


public class MapsActivity  extends Activity implements TextToSpeech.OnInitListener
{
    GPSTracker gps;
    double latitude,longitude;
    private GoogleMap googleMap;
    TextToSpeech talker;
    private static final String TAG = "Blind Navigation";
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;

    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final boolean D = true;
    private static String address = "30:14:10:13:12:56";
    public static final int MESSAGE_READ = 2;
    public static final String bluetoothTitle = "Enable Bluetooth Settings";
    public static final String gpsTitle = "Enable GPS Settings";
    public static final String bluetoothMessage = "Bluetooth is not enabled. Do you want to go to settings menu?";
    public static final String gpsMessage = "GPS is not enabled. Do you want to go to settings menu?";
    private PowerManager.WakeLock wl;
    private BluetoothDataReader mReadThread;
    private static final int REQUEST_ENABLE_BT = 1;
    TextView tittle,bluetoothaddText,bluetoothNameText;
    ImageView display;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }


        // Getting a WakeLock. This insures that the phone does not sleep
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
        bluetoothaddText = (TextView) findViewById(R.id.bluetoothtextView);
        bluetoothNameText = (TextView) findViewById(R.id.deviceNameTextview);
        bluetoothaddText.setText(address);

        // check local bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // enable bluetooth
        if (!mBluetoothAdapter.isEnabled()) {


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);

            // Setting Dialog Title
            alertDialog.setTitle(bluetoothTitle);

            // Setting Dialog Message
            alertDialog.setMessage(bluetoothMessage);

            // On pressing Settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();


        }
        //tts in oncreate
        talker = new TextToSpeech(this, this);
        final Handler h = new Handler();
        //int delay = 1000; //milliseconds

        h.postDelayed(new Runnable() {
            public void run() {
                gps = new GPSTracker(MapsActivity.this);

                // check if GPS enabled
                if (gps.canGetLocation()) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                }
                else {
                    gps.showSettingsAlert();
                }

                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                int source = 0, dest = 0;

                if (latitude <= lat_long[1][0] && latitude >= lat_long[0][0] && longitude >= lat_long[0][1] && longitude <= lat_long[1][1]) {
                    if (source == 0) {
                        //Welcome to SVCE
                        //Move forward
                        source = 1;
                    }
                }
                if (latitude <= lat_long[18][0] && latitude >= lat_long[20][0] && longitude >= lat_long[18][1] && longitude <= lat_long[20][1]) {
                    //Circle : Move towards your right in circular fashion
                }
                if (latitude <= lat_long[22][0] && latitude >= lat_long[24][0] && longitude >= lat_long[24][1] && longitude <= lat_long[22][1]) {
                    //Circle : Move towards your left in circular fashion
                }
                if (latitude <= lat_long[26][0] && latitude >= lat_long[28][0] && longitude >= lat_long[26][1] && longitude <= lat_long[28][1]) {
                    //Stairs ahead : Move Up
                    //Move forward
                }
                if (latitude <= lat_long[30][0] && latitude >= lat_long[31][0] && longitude >= lat_long[32][1] && longitude <= lat_long[30][1]) {
                    //Turn Right
                    //Move forward
                }
                if (latitude <= lat_long[34][0] && latitude >= lat_long[35][0] && longitude >= lat_long[36][1] && longitude <= lat_long[34][1]) {
                    //Stairs ahead : Move Down
                }
                if (latitude <= lat_long[38][0] && latitude >= lat_long[40][0] && longitude >= lat_long[38][1] && longitude <= lat_long[39][1]) {
                   if(dest==0) { //Reached Library
                       v.vibrate(1000);
                       dest=1;
                   }
                }

                h.postDelayed(this, 1000);
            }
        }, 1000);

    }
    @Override
    public void onInit(int status)
    {

        say("Welcome to Blind Navigation Application");

    }

    private void say(String text2say)
    {
        // TODO Auto-generated method stub
        talker.speak(text2say, TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    public void onStart() {
        super.onStart();
        wl.acquire();

    }

    @Override
    public void onResume() {
        super.onResume();
        initilizeMap();

        startDeviceConnection();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_ENABLE_BT) {
//            startDeviceConnection();
        }

    }


    public void startDeviceConnection() {

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        // create a bluetooth socket
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "ON RESUME: Socket creation failed.", e);
        }
        bluetoothNameText.setText(device.getName());

        mBluetoothAdapter.cancelDiscovery();

        try {
            btSocket.connect();
            Log.e(TAG,
                    "ON RESUME: BT connection established, data transfer link open.");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.e(TAG,
                        "ON RESUME: Unable to close socket during connection failure",
                        e2);
            }
        }
        mReadThread = new BluetoothDataReader(btSocket, this, mHandler);
        if (mReadThread.isAlive() == false) {
            mReadThread.start();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
        mReadThread.cancel();
        mReadThread.stop();

        // Release the WakeLock so that the phone can go to sleep to preserve
        // battery.
        wl.release();

    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMesage = new String(readBuf, 0, msg.arg1);

                    if("1".equals(readMesage))
                    {

                        String hodCabin="Now you arrived at HOD Cabin";
                        bluetoothaddText.setText(hodCabin);
                        talker.speak(hodCabin, TextToSpeech.QUEUE_ADD, null);
                    }
                    else if("2".equals(readMesage))
                    {

                        String rndtext="Now you arrived at R & D Lab";
                        bluetoothaddText.setText(rndtext);
                        talker.speak(rndtext, TextToSpeech.QUEUE_ADD, null);
                    }
                    else if("3".equals(readMesage))
                    {

                        String strhoi="Now you arrived at Library";
                        bluetoothaddText.setText(strhoi);
                        talker.speak(strhoi, TextToSpeech.QUEUE_ADD, null);
                    }
                    else if("4".equals(readMesage))
                    {

                        String strhoi="Now you arrived at Principal Cabin";
                        bluetoothaddText.setText(strhoi);
                        talker.speak(strhoi, TextToSpeech.QUEUE_ADD, null);
                    }

                    else if("5".equals(readMesage))
                    {

                        String strhoi=" Be Careful Object Detected";
                        bluetoothaddText.setText(strhoi);
                        talker.speak(strhoi, TextToSpeech.QUEUE_ADD, null);
                    }

                    break;

            }
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            googleMap.setMyLocationEnabled(true);

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    double[][] lat_long=new double[][]{
            //welcome to svce
            {13.16030, 77.63615},
            {13.16033, 77.63630},
            {13.16030, 77.63615},
            {13.16033, 77.63630},

            //circle

            {13.15994,	77.63621},
            {13.15994,	77.63625},
            {13.15988,	77.63625},
            {13.15988,	77.63621},

            //path right
            {13.16032,	77.63618},
            {   13.15998,	77.63617},

            {13.15995,	77.63615},
            {13.15985,	77.63615},
            {13.15982,	77.63618},

            //left
            {13.16034,	77.63629},
            {13.16001,	77.63629},

            {13.15996,	77.63633},
            {13.15986,	77.63633},
            {13.15981,	77.63630},

            //Before Circl Right

            {13.16000,	77.63616},
            {13.16000,	77.63623},
            {13.15996,	77.63623},
            {13.15996,	77.63616},

            //left

            {   13.16000,	77.63630},
            {   13.16000,	77.63623},
            {   13.15996,	77.63623},
            {   13.15996,	77.63633},

            //Before Stairs
            {   13.15984,	77.63618},
            {   13.15984,	77.63630},
            {   13.15982,	77.63630},
            {   13.15982,	77.63618},


            //Right turn
            {   13.15964,	77.63626},
            {   13.15960,	77.63626},
            {   13.15960,	77.63623},
            {   13.15964,	77.63623},


            //2nd Stairs
            {13.15962,	77.63616},
            {13.15959,	77.63616},
            {13.15959,	77.63611},
            {13.15962,	77.63611},

            //Library
            {13.15968,	77.63606},
            {13.15968,	77.63612},
            {13.15962,	77.63606},
            {13.15962,	77.63612}
    };

}

