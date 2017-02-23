package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

import android.bluetooth.BluetoothDevice;

/**
 * @author KI
 * @version 1.0
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
