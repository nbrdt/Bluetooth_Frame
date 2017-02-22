package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;

import java.util.List;

/**
  *@author kevin
  *@version 0.0 from 09.02.2017.02.2017 in Bluetooth_Frame
 **/
public interface BluetoothClient {
    public List<String> getAddressesAndNames(boolean forceReset) throws BluetoothInactivityException;
    public void connectBT(String AddressAndName, int tries) throws BluetoothConnectionStateException;
    public void disconnect() throws BluetoothConnectionStateException;
    public void write(String toWrite) throws BluetoothConnectionStateException;
    public int read (char[] buffer) throws BluetoothConnectionStateException;
}
