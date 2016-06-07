package com.myapp.re_war.svce;


        import android.bluetooth.BluetoothSocket;
        import android.content.Context;
        import android.os.Handler;
        import android.util.Log;

        import java.io.IOException;
        import java.io.InputStream;

public class BluetoothDataReader extends Thread
{
    private static final String TAG = null;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    static String readMessage;
    private final Handler mHandler;

    public BluetoothDataReader(BluetoothSocket socket,Context context, Handler handler) {
        Log.d(TAG, "create ConnectedThread");
        mmSocket = socket;
        mHandler = handler;
        InputStream tmpIn = null;


        // Get the BluetoothSocket input streams
        try {
            tmpIn = mmSocket.getInputStream();

        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mmInStream = tmpIn;

    }


    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                readMessage = new String(buffer, 0, bytes);
                mHandler.obtainMessage(MapsActivity.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
                // Send the obtained bytes to the UI Activity

            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);

                break;
            }
        }
    }


    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }

}

