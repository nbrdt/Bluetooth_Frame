package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;
/**@author kevin
  *@version 0.0 from 09.02.2017 in Bluetooth_Frame
 **/


import java.util.List;

public interface ArduinoBluetoothClient extends BluetoothClient {
    public List<DataSet> getReceivedData();
    public void clearReceivedData() ;
}
