package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

import android.bluetooth.BluetoothDevice;

/**
 * Created by kevin on 19.01.2017.
 */

interface BluetoothListener  {
    public void onStart();
    public void onStop();
    public void onConnect();
    public void onSend();
    public void onConnectionEstablish(BluetoothDevice connectedDevice);
    public void onConnectFailure(boolean closed);
    public void onConnectionLoss();
    public void onConnectionTermination();
}
