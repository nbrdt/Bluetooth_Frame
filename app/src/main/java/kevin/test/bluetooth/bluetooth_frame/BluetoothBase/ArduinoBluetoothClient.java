package kevin.test.bluetooth.bluetooth_frame.BluetoothBase;
/**
 * This Interface defines specific Methods to be implemented by an subclass of any BluetoothClient
 * The Arduino BluetoothClient should contain some kind of Timer which decodes in specified Intervals data Send by the Arduino
 * Received Data is specified as a List, to provide platform independency and a possibility to change internal List represenations,
 * which might be necessary to increase System Perfomance
 *
 *@author KI
 *@version 1.0
 **/
import java.util.List;

public interface ArduinoBluetoothClient extends BluetoothClient {
  /**
   * Returns all identified Data since the last Timer-Event fired, but will not clear the internal List.
   * The internal representation will be copied, so that you do not need to worry about Data being modified in an unexpected moment.
   * This therefore decreases Performance to at least the Number of Dataset's, which has been recieved since the last call of clearReceivedData.
   * Consequently, it is recommended to Read and clear the Data in regular intervals.
   *
   * @return List of DataSet which represents the received Data, in case of no received Data, this will return a null Object reference
   **/
    public List<DataSet> getReceivedData();

  /**
   * Clears the internal representation of the receivedData List.
   * Warning: it is not checked, whether or not the Data has been outputed or not, you might therefore lose Data, if you don't call the receive Data Method before.
   *
   * Because of heavy workload required by the getReceivedData Method,m it is recommended to Read and clear the Data in regular intervals.
  **/    
    public void clearReceivedData() ;
}
